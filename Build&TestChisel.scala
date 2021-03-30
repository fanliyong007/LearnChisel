package mypack//由于chisel来源于scala而scala又来源于Java，故都是采用了同样的包机制来管理项目
import chisel3._ //引入chisel必要的类与模块
class Abc extends Module{
    val io=IO(new Bundle())
}
//当在别的地方想要使用Abc这个类的时候可以这样做
import mypack._ //下划线表示所有mypack中的类都会被引用
class AbcUser extends Module{
    val io=IO(new Bundle{})
    val abc=new Abc()
}
//当然也可以不用全部引用而采用（包名 点 类名）的方式来访问
class AbcUser2 extends Module{
    val io=IO(new Bundle{})
    val abc=new mypack.Abc()
}
//引用单个类也可以在import中直接规定
import mypack.Abc
class AbcUser3 extends Module{
    val io=IO(new Bundle{})
    val abc=new Abc()
}

//可以通过简单的sbt命令来编译并执行一个Chisel项目
$ sbt run//所有的对象都会被列出以及可选
//当然也可以直接将需要编译的对象作为参数传入sbt命令之中
$ sbt "runMain mypacket.MyObject"
//因为sbt只会搜索main部分的源文件树故在测试的时候需要加入test树源文件部分
$ sbt "test:runMain mypacket.MyTester"

//测试硬件一般叫做testbench，Chisel提供的testbench叫做PeekPokeTester,使用时需要引入如下的包
import chisel3._
import chisel3.iotesters._
//测试电路需要包括以下三部分：
//1、接受测试的器件（称为DUT）
//2、测试逻辑（同样称为testbench）
//3、包含main函数的测试对象
class DeviceUnderTest extends Module{
    val io=IO(new Bundle{
        val a=Input(UInt(2.W))
        val b=Input(UInt(2.W))
        val out=Output(UInt(2.W))
    })
    io.out := io.a & io.b
}//被测试对象为一个与逻辑门
//每个测试的dut都需要继承PeekPokeTest并把需要测试的dut作为构造器
class TesterSimple(dut:DeviceUnderTest) extends PeekPokeTest(dut){
    poke(dut.io.a,0.U)
    poke(dut.io.b,1.U)
    step(1)
    println("Result is:"+peek(dut.io.out).toString)
    poke(dut.io.a,3.U)
    poke(dut.io.b,2.U)
    step
    println("Result is:"+peek(dut.io.out).toString)
}