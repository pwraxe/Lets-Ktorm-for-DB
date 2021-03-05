package com.codexdroid

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json
import org.ktorm.database.Database
import org.ktorm.dsl.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


private var personList = mutableListOf<Person>()


@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    val url = "jdbc:mysql://localhost/test"
    val user = "root"
    val pass = ""


    val dataConnect : Database = Database.connect(url = url, driver = "com.mysql.jdbc.Driver", user, pass)
    lateinit var person : Person
    lateinit var landmark: Landmark

    var list = ArrayList<Person>()

    lateinit var personList: PersonList

    install(ContentNegotiation){
        json(Json {
            this.prettyPrint = true
            this.isLenient = true
            this.ignoreUnknownKeys = true
        })
    }

    routing {

        //save data to server database
        post("/add"){
            try {
                person = call.receive<Person>()
                landmark = person.landmark

                dataConnect.insert(Human){
                    set(Human.id,person.id)
                    set(Human.name,person.name)
                    set(Human.email,person.email)
                    set(Human.mobile,person.mobile)
                }
                dataConnect.insert(Address){
                    set(Address.id,landmark.id)
                    set(Address.area, landmark.area)
                    set(Address.town, landmark.town)
                    set(Address.city, landmark.city)
                    set(Address.state, landmark.state)
                    set(Address.pincode, landmark.pincode)
                }

                call.respond(ManipulationResponse(HttpStatusCode.Accepted.value,"Data Saved",true))
            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Internal Server Error : $ex",false))
            }
        }

        //get single record from id
        get("/{id}"){
            val id = call.parameters["id"] ?: return@get call.respond(FailResponse(HttpStatusCode.BadRequest.value,"Bad Request",true))

            val query = dataConnect
                .from(Human)
                .crossJoin(Address,Human.id eq id.toInt())
                .select()
                .where { Human.id eq Address.id }

            query.forEach { row->
                landmark = Landmark(
                    row[Address.id].toString().toInt(),
                    row[Address.area].toString(),
                    row[Address.town].toString(),
                    row[Address.city].toString(),
                    row[Address.state].toString(),
                    row[Address.pincode].toString().toInt())
                person = Person(
                    row[Human.id].toString().toInt(),
                    row[Human.name].toString(),
                    row[Human.email].toString(),
                    row[Human.mobile].toString().toLong(),
                    landmark)
            }

            if(query.totalRecords == 1){
                call.respond(SingleDataSuccessResponse(HttpStatusCode.Found.value,"Found ${query.totalRecords} Record(s) ",true,person))
            }else{
                call.respond(FailResponse(HttpStatusCode.NotFound.value,"No Record Found",false))
            }
        }

        //get all data from both/different table
        get("/all"){
            list.clear()
            try {

                for(row in dataConnect.from(Human).crossJoin(Address , on = Human.id eq Address.id).select()){

                    landmark = Landmark(
                        row[Address.id].toString().toInt(),
                        row[Address.area].toString(),
                        row[Address.town].toString(),
                        row[Address.city].toString(),
                        row[Address.state].toString(),
                        row[Address.pincode].toString().toInt())

                    person = Person(
                        row[Human.id].toString().toInt(),
                        row[Human.name].toString(),
                        row[Human.email].toString(),
                        row[Human.mobile].toString().toLong(),landmark)

                    list.add(person)

                }
                personList = PersonList(list)

                call.respond(SuccessResponse(HttpStatusCode.Created.value,"All Your Data",true,personList))

            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Internal Server Error : $ex",false))
            }
        }

        put("/update") {
            try {

                person = call.receive<Person>()
                dataConnect.update(Human){
                    set(Human.name,person.name)
                    set(Human.email,person.name)
                    set(Human.mobile,person.mobile)
                    where { Human.id eq person.id }
                }

                dataConnect.update(Address){
                    set(Address.area,person.landmark.area)
                    set(Address.town,person.landmark.town)
                    set(Address.city,person.landmark.city)
                    set(Address.state,person.landmark.state)
                    set(Address.pincode,person.landmark.pincode)
                    where { Address.id eq person.landmark.id }
                }

                call.respond(ManipulationResponse(HttpStatusCode.OK.value,"Both Table Updated",true))

            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Internal Server Error : $ex",false))
            }
        }

        delete("/delete/{id}") {
            try {

                val id = call.parameters["id"] ?: return@delete call.respond(FailResponse(HttpStatusCode.NotFound.value,"Invalid Request",false))
                dataConnect.delete(Human){ it.id eq id.toInt() }
                dataConnect.delete(Address){ it.id eq id.toInt() }
                call.respond(ManipulationResponse(HttpStatusCode.OK.value,"Data Deleted in Both Table",true))

            }catch (ex : Exception){
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value,"Internal Server Error : $ex",false))
            }


        }
    }
}
