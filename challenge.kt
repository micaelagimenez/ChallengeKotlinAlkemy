import java.util.Calendar

/*
 * Parkable class allows to check out vehicles from parking lot
 * and calculate the checking out fee
*/

data class Parkable(
    var vehicle: Vehicle,
    var checkInTime: Calendar,
    var parking: Parking){

    //get time since vehicle was in the parking lot
    val parkedTime: Long
        get() = (Calendar.getInstance().timeInMillis - checkInTime.timeInMillis) / 60000 //minutes in milliseconds

    fun checkOutVehicle(plate:String) {

        //evaluate check out result
        fun onSuccess(finalFee: Int): String {
            return "Your fee is $finalFee. Come back soon."
        }

        fun onError(): String {
            return "Sorry, the check-out failed"
        }

        //check if there is a vehicle with given plate
        val vehiclesPlate = parking.vehicles.find{ it.plate == plate }
        if (vehiclesPlate != null) {
            val finalFee = calculateFee(
                vehicleType = vehiclesPlate.vehicleType,
                parkedTime = vehiclesPlate.parkedTime.toInt(),
                hasDiscountCard = !vehiclesPlate.discountCard.isNullOrEmpty())

            //tell client their final fee
            onSuccess(finalFee)

            //remove vehicle given plate
            parking.vehicles.remove(vehiclesPlate)
        } else {
            //show error when vehicle's plate is not found
            onError()
        }

    }


    //calculate final fee after check out
    fun calculateFee(vehicleType: VehicleType, parkedTime:Int, hasDiscountCard:Boolean): Int{
        //fees
        var finalFee = 0
        var carFee = 20
        var motorcycleFee = 15
        var minibusFee = 25
        var busFee = 30

        if(parkedTime<121){

            if(vehicleType.price == 20){
                finalFee += carFee
            } else if (vehicleType.price == 15){
                finalFee += motorcycleFee
            } else if (vehicleType.price == 25){
                finalFee += minibusFee
            } else {
                finalFee += busFee
            }

        } else {
            val extraFee = 5
            //works but not with fraction of 15 minutes
            if(vehicleType.price == 20){
                finalFee = carFee
                finalFee += ((parkedTime - 120) / 15) * extraFee

            } else if (vehicleType.price == 15){
                finalFee = motorcycleFee
                finalFee += ((parkedTime - 120) / 15) * extraFee
            } else if (vehicleType.price == 25){
                finalFee = minibusFee
                finalFee += ((parkedTime - 120) / 15) * extraFee
            } else {
                finalFee = busFee
                finalFee += ((parkedTime - 120) / 15) * extraFee
            }
        }

        //add discount 15% if they have discount card
        if(hasDiscountCard){
            val discount = (finalFee * 15) / 100
            finalFee = finalFee - discount
            return finalFee
        }
        //return final fee without discount
        return finalFee
    }

}



/*
 * Parking class allows to add vehicles to the parking lot,
 * list the cars inside and show the parking's revenue
*/

data class Parking(val vehicles: MutableSet<Vehicle>){

    //limit amount of vehicles to 20
    fun addVehicle(vehicle: Vehicle): Boolean{

        if(vehicles.size <= 19){
            vehicles.add(vehicle)
            println( "Welcome to AlkeParking!")
            return true
        } else {
            println("Sorry, the check-in failed" )
            return false
        }
    }

    //list all the cars in the parking
    fun listVehicles(vehicles: MutableSet<Vehicle>){
        for(i in vehicles){
            var plate = i.plate
            println("Parked vehicle $plate")
        }
    }

    var revenue: Pair<Int,Int> = Pair(0,0)
    var countVehiclesCheckedOut = 0
    var totalRevenue = 0

    //shows parking's revenue
    fun revenue(vehicle: Vehicle):String{

        val parkable = Parkable(vehicle,java.util.Calendar.getInstance(),parking = Parking(vehicles))
        parkable.checkOutVehicle(vehicle.plate)
        countVehiclesCheckedOut += 1
        totalRevenue += parkable.calculateFee(vehicleType = vehicle.vehicleType,
            parkedTime = vehicle.parkedTime.toInt(),
            //seems to pass the discount anyway
            hasDiscountCard = !vehicle.discountCard.isNullOrEmpty())
        revenue = revenue.copy(countVehiclesCheckedOut, totalRevenue)


        var revenueCountVehicles = revenue.first
        var revenueTotalAmount = revenue.second

        return "$revenueCountVehicles vehicles have checked out and have earnings of $revenueTotalAmount"
    }
}

/*
 * Vehicle class determines the necessary data for each vehicle
*/

data class Vehicle(val plate: String,
                   var vehicleType: VehicleType,
                   var checkInTime: Calendar,
                   var discountCard:String?=" "){

    override fun equals(other: Any?): Boolean{
        if(other is Vehicle){
            return this.plate == other.plate
        }
        return super.equals(other)
    }

    //return hashed plate
    override fun hashCode(): Int{
        return this.plate.hashCode()
    }

    //get time since vehicle was in the parking lot
    val parkedTime: Long
        get() = (Calendar.getInstance().timeInMillis - checkInTime.timeInMillis) / 60000 //minutes in milliseconds
}

/*
 * Enum class VehicleType enumerates the different types of vehicles
*/

enum class VehicleType(val price: Int){
    CAR(20),
    MOTORCYCLE(15),
    MINIBUS(25),
    BUS(30)
}


/*
 * Main function allows to check the functionality of the previous classes
*/

fun main(){
    //first vehicles to use in the next checks
    val car = Vehicle("AA11AA", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_001")
    val moto = Vehicle("822888", VehicleType.MOTORCYCLE, Calendar.getInstance())
    val minibus = Vehicle("CC333CC", VehicleType.MINIBUS, Calendar.getInstance())
    val bus = Vehicle("004400", VehicleType.BUS, Calendar.getInstance(), "DISCOUNT_CARD_002")
    val parking0 = Parking(mutableSetOf(car, moto, minibus, bus))

    //check if parking works
    println("Check to see if parking works when adding a vehicle: " + parking0.vehicles.contains(car))
    println("Check 2 to see if parking works when adding a vehicle: " + parking0.vehicles.contains(moto))

    //check for duplicates
    val car12 = Vehicle("AA11AA", VehicleType.CAR, Calendar.getInstance())
    val isCar12Inserted = parking0.vehicles.add(car12)
    println("Check to see if duplicates are added: " + isCar12Inserted)

    //remove element from set
    parking0.vehicles.remove(moto)

    //20 vehicles
    val car1 = Vehicle("AA13DD", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_003")
    val moto1 = Vehicle("82DDF", VehicleType.MOTORCYCLE, Calendar.getInstance())
    val minibus1 = Vehicle("CCDDFC", VehicleType.MINIBUS, Calendar.getInstance())
    val bus1 = Vehicle("004SD0", VehicleType.BUS, Calendar.getInstance(), "DISCOUNT_CARD_004")
    val car2 = Vehicle("AAEFGA", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_005")
    val moto2 = Vehicle("83888", VehicleType.MOTORCYCLE, Calendar.getInstance())
    val minibus2 = Vehicle("CC3FF", VehicleType.MINIBUS, Calendar.getInstance())
    val bus2 = Vehicle("00GFG", VehicleType.BUS, Calendar.getInstance(), "DISCOUNT_CARD_006")
    val car3 = Vehicle("ABVVA", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_007")
    val moto3 = Vehicle("VBVB88", VehicleType.MOTORCYCLE, Calendar.getInstance())
    val minibus3 = Vehicle("CC4443CC", VehicleType.MINIBUS, Calendar.getInstance())
    val bus3 = Vehicle("00BFBF", VehicleType.BUS, Calendar.getInstance(), "DISCOUNT_CARD_008")
    val car4 = Vehicle("DFDFD", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_009")
    val moto4 = Vehicle("8GFG88", VehicleType.MOTORCYCLE, Calendar.getInstance())
    val minibus4 = Vehicle("FDFS34", VehicleType.MINIBUS, Calendar.getInstance())
    val bus4 = Vehicle("DFD3345", VehicleType.BUS, Calendar.getInstance(), "DISCOUNT_CARD_010")
    val car5 = Vehicle("34234GFF", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_011")
    val moto5 = Vehicle("FFD34", VehicleType.MOTORCYCLE, Calendar.getInstance())
    val minibus5 = Vehicle("FGFG55", VehicleType.MINIBUS, Calendar.getInstance())
    val bus5 = Vehicle("FGFGBC4", VehicleType.BUS, Calendar.getInstance(), "DISCOUNT_CARD_012")
    val bus6 = Vehicle("FGFGBC4", VehicleType.BUS, Calendar.getInstance())

    //add elements to the mutableSet with addVehicle
    val vehiclesToCheckIn = mutableSetOf<Vehicle>()
    val parking = Parking(vehiclesToCheckIn)

    val vehiclesSetToAdd = mutableSetOf(car1, moto1, minibus1, bus1,
        car2, moto2, minibus2, bus2, car3, moto3, minibus3, bus3,
        car4, moto4, minibus4, bus4, car5, moto5, minibus5, bus5, bus6, bus, moto, minibus)

    println(" ")
    println("Check to see if adding elements to set works:")
    for(i in vehiclesSetToAdd){
        parking.addVehicle(i)
    }

    //check if calculateFee works
    println(" ")
    val bus46 = Vehicle("FGFGBASC4", VehicleType.BUS, Calendar.getInstance())
    var parkable = Parkable(bus46, java.util.Calendar.getInstance(),parking)
    println("Check to see calculate Average Minibus Fee: "+ parkable.calculateFee(vehicleType = VehicleType.MINIBUS,parkedTime = 120,hasDiscountCard = false))
    println("Check to see calculate Average Minibus Fee with discount: "+ parkable.calculateFee(vehicleType = VehicleType.MINIBUS,parkedTime = 120,hasDiscountCard = true))
    println("Check to see calculate Average Car Fee: "+ parkable.calculateFee(vehicleType = VehicleType.CAR,parkedTime = 120,hasDiscountCard = false))
    println("Check to see calculate 2:30hs Bus Fee: "+ parkable.calculateFee(vehicleType = VehicleType.BUS,parkedTime = 150,hasDiscountCard = false))

    //check if list works
    println(" ")
    println("Check to see if list of vehicles in parking lot works: ")
    println(parking.listVehicles(vehicles = parking.vehicles))

    //check revenue
    println(" ")
    val car22 = Vehicle("FGASFGBC4", VehicleType.CAR, Calendar.getInstance())
    val car23 = Vehicle("FDDGFGBC4", VehicleType.CAR, Calendar.getInstance(), "DISCOUNT_CARD_20")
    println("Check revenue: " + parking.revenue(car22))
    println("Check revenue: " + parking.revenue(car23))
}