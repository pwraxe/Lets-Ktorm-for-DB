package com.codexdroid

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.io.File

@Serializable
data class Person(
    var id : Int,
    var name : String,
    var email : String,
    var mobile : Long,
    var landmark: Landmark
)

@Serializable
data class PersonList(var list : ArrayList<Person>)

@Serializable
data class FailResponse(
    var status : Int,
    var message : String,
    var isSuccess : Boolean
)


@Serializable
data class ManipulationResponse(
    var status: Int,
    var message: String,
    var isSuccess: Boolean
)

@Serializable
data class SuccessResponse(
    var status: Int,
    var message: String,
    var isSuccess: Boolean,
    var persons: PersonList
)


@Serializable
data class Landmark(
    var id : Int,
    var area : String,
    var town : String,
    var city : String,
    var state : String,
    var pincode : Int,
)




@Serializable
data class SingleDataSuccessResponse(
    var status: Int,
    var message: String,
    var isSuccess: Boolean,
    var person: Person
)




@Serializable
data class SuccessFileResponse(
    var status: Int,
    var message: String,
    var isSuccess: Boolean,
    @Contextual
    var file : File
)






//When you use ktorm then describes your table structure
object Human : Table<Nothing>("human"){
    val id = int("id").primaryKey()
    val name = varchar("name")
    val email = varchar("email")
    var mobile = long("mobile")

}

object Address : Table<Nothing>("address"){
    var id = int("id").primaryKey()
    var area = varchar("area")
    var town = varchar("town")
    var city = varchar("city")
    var state = varchar("state")
    var pincode = int("pincode")
}

