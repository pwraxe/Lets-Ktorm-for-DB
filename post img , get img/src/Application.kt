package com.codexdroid

import com.google.gson.Gson
import com.sun.net.httpserver.Authenticator
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.json.Json
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


private var personList = mutableListOf<Person>()
private var FILE_PATH = "C:\\Users\\AKSHAY\\Desktop\\ktor_images"

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

        post("/img"){
            try {

                val multipart = call.receiveMultipart()

                multipart.forEachPart { part ->

                    if(part is PartData.FileItem){

                         val fileName = part.originalFileName!!

                        val file : File = if (fileName.endsWith(".jpg") ||
                            fileName.endsWith(".png") ||
                            fileName.endsWith(".jpeg") ||
                            fileName.endsWith(".gif")) {
                                println("if----> $FILE_PATH\\$fileName")
                                File("$FILE_PATH\\$fileName")

                        }else{
                            val ext = File(part.originalFileName).extension
                            println("else ----> $FILE_PATH\\${part.originalFileName}.$ext")
                            File("$FILE_PATH\\${part.originalFileName}.$ext")
                        }

                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { buffer ->
                                input.copyTo(buffer)
                            }
                        }
                     }
                    part.dispose()
                }

            }catch (ex : Exception){
                call.respond(ex)
            }
        }


        get("/{name}"){

            val name = call.parameters["name"]!! // ?: call.respond(FailResponse(HttpStatusCode.NotAcceptable.value,"Invalid Parameter",false))

            println("img Name : $name")
            if(name.endsWith(".jpg") ||
                    name.endsWith("jpeg") ||
                    name.endsWith("png") ||
                    name.endsWith("gif")){

                val file = File("$FILE_PATH\\$name")
                if(file.exists()){
                    call.respondFile(file)
                }
            }else{
                val file = File("$FILE_PATH")
                var fileLists = file.list()

                println("${Gson().toJson(fileLists)}")

                loop@for(imgfile in fileLists){
                    if(name == imgfile){
                        call.respondFile(file)
                        break@loop
                    }
                }
                call.respond(FailResponse(HttpStatusCode.NotFound.value,"No Such Image",false))
            }



        }
    }
}