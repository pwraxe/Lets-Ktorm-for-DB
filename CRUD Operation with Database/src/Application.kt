package com.codexdroid

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.util.Identity.decode
import io.ktor.utils.io.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.modules.SerializersModule
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.sql.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


private var personList = mutableListOf<Person>()


@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    val url = "jdbc:mysql://localhost/test"
    val user = "root"
    val pass = ""


    val dataConnect : Database = Database.connect(url = url, driver = "com.mysql.jdbc.Driver", user, pass)
    lateinit var person : Person

    install(ContentNegotiation){
        json(Json {
            this.prettyPrint = true
            this.isLenient = true
            this.ignoreUnknownKeys = true

        })
    }


    routing {

        //Get Data from Client and store it in DB
        post{

            person = call.receive<Person>()
            try {
                dataConnect.useTransaction {
                    dataConnect.insert(Human) {
                        set(it.id,person.id)
                        set(it.name,person.name)
                        set(it.email,person.email)
                        set(it.mobile,person.mobile)
                    }
                }
                //call.respond(SuccessResponse(HttpStatusCode.Accepted.value,"Data Inserted",true, personList))
            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.BadRequest.value,"Error to Insert : $ex",false))
            }
        }

        //get multiple data and store it in DB
        post("/multiple"){

            try {


                // you received array
                val persons = call.receive<ArrayList<Person>>()

                dataConnect.batchInsert(Human) {

                    for (data in persons) {

                        item {
                            set(it.id, data.id)
                            set(it.name, data.name)
                            set(it.email, data.email)
                            set(it.mobile, data.mobile)
                        }
                    }
                }

                call.respond(FailResponse(HttpStatusCode.Created.value,"Multiple Data Inserted",true))

            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.NotAcceptable.value,"Error to Insert Data $ex",false))
                println("Exception : $ex")
            }
        }

        //fetch all data from database
        get("/all"){
            personList.clear()
            try {
                for(row in dataConnect.from(Human).select()){
                    personList.add(Person(
                        row[Human.id].toString().toInt(),
                        row[Human.name].toString(),
                        row[Human.email].toString(),
                        row[Human.mobile].toString().toLong()
                    ))
                }

                if(personList.isNotEmpty()){
                    call.respond(SuccessResponse(HttpStatusCode.Created.value,"Your ${personList.size} Data",true, personList))
                }else{
                    call.respond(FailResponse(HttpStatusCode.NotFound.value,"No Data at Server",false))
                }
            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Error : $ex",false))
            }
        }

        //get single row from mysql
        get("/{id}"){
            try {

                val id = call.parameters["id"] ?: return@get call.respond(FailResponse(HttpStatusCode.NotAcceptable.value, "Wrong Parameter", false))

                val query = dataConnect
                    .from(Human)
                    .select()
                    .where { Human.id eq id.toInt() }

                query.forEach { row->
                    person = Person(
                        row[Human.id].toString().toInt(),
                        row[Human.name].toString(),
                        row[Human.email].toString(),
                        row[Human.mobile].toString().toLong())
                    }

                if(query.totalRecords == 1){  // query found 1 record for given condition
                    call.respond(SingleDataSuccessResponse(HttpStatusCode.Found.value,"We have Record",true,person))
                }else{
                    call.respond(FailResponse(HttpStatusCode.NotFound.value,"No Records Found for given ID $id",false))
                }
            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Error : $ex",false))
            }
        }

        //update data/row
        put("/update"){

            try {

                val person = call.receive<Person>()

                dataConnect.update(Human) {
                    set(Human.name, person.name)
                    set(Human.email, person.email)
                    set(Human.mobile, person.mobile)
                    where { it.id eq person.id }
                }
                call.respond(SuccessResponse(HttpStatusCode.OK.value,"Data Updated",true, personList))

            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Internal Server Error : $ex",false))
            }

        }

        //delete data/row
        delete("/delete/{id}") {
            try {

                val id = call.parameters["id"] ?: return@delete call.respond(FailResponse(HttpStatusCode.NotAcceptable.value,"Invalid Request",false))
                dataConnect.delete(Human) { it.id eq id.toInt() }
                call.respond(SuccessResponse(HttpStatusCode.OK.value,"Data Deleted",true, personList))

            }catch (ex : Exception){

                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Internal Server Error",false))

            }

        }


    }
}