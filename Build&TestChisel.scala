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



//每个硬件组件都是由Module继承而来，并继承了Module的接口，
//其中包括一个用Bundle来定义的io域，即用来控制输入输出部分
//（此处应有一张电路图来自Chisel-book Figure6.1)
class CompA extends Module{
    val io=IO(new Bundle{
        val a=Input(UInt(8.W))
        val b=Input(UInt(8.W))
        val x=Output(UInt(8.W))
        val y=Output(UInt(8.w))
    })//对继承的接口进行重写
    //接下来就是对内部数字逻辑设计的定义部分
}
class CompB extends Module{
    val io=IO(new Bundle{
        val in1=Input(UInt(8.W))
        val in2=Input(UInt(8.W))
        val out=Output(UInt(8.W))
    })
    //..........
}
//CompC组件由CompA与CompB连接而成，模块化设计让逻辑组件也有了软件代码的代码复用性
class CompC extends Module{
    val io=IO(new Bundle{
        val in_a=Input(UInt(8.W))
        val in_b=Input(UInt(8.W))
        val in_c=Input(UInt(8.W))
        val out_x=Output(UInt(8.W))
        val out_y=Output(UInt(8.W))
    })
    //创建compA与compB元件
    val compA=Module(new CompA())
    val compB=Module(new CompB())
    //连接A元件
    compA.io.a := io.in_a//输入部分
    compA.io.b := io.in_b
    io.out_x := compA.io.x//输出部分
    //连接B元件
    compB.io.in1 := compA.io.y//将compA与compB连接
    compB.io.in2 := io.in_c//将compB与compC连接
    io.out_y := compB.io.out//将输出与compB的输出相连接
}
class CompD extends Module{
    val io=IO(new Bundle{
        val in=Input(UInt(8.W))
        val out=Output(UInt(8.W))
    })
}
//最后由CompC与CompD组成完整的逻辑设计
class TopLevel extends Module{
    val io=IO(new Bundle{
        val in_a=Input(UInt(8.W))
        val in_b=Input(UInt(8.W))
        val in_c=Input(UInt(8.W))
        val out_m=Output(UInt(8.W))
        val out_n=Output(UInt(8.W))
    })
    //创建c与d元件
    val c=Module(new CompC())
    val d=Module(new CompD())
    //连接c
    in_a := io.in_a
    in_b := io.in_b
    in_c := io.in_c
    io.out_m := c.io.out_x
    //连接d
    d.io.in := c.io.out_y
    io.out_n := d.io.out
}

//16位ALU设计（输入输出16位控制信号两位）
class ALU extends Module{
    val io=IO(new Bundle{
        val a=Input(UInt(16.W))
        val b=Input(UInt(16.W))
        val fn=Input(UInt(2.W))
        val y=Output(UInt(16.W))
    })
    //设置默认输出
    io.y := 0.U 
    //对于不同输入的处理
    switch(io.fn){
        is(0.U){ io.y := io.a + io.b }
        is(1.U){ io.y := io.a - io.b }
        is(2.U){ io.y := io.a | io.b }
        is(3.U){ io.y := io.a & io.b }
    }
}

//使用<> 连接运算符进行整体连接
//可将相同名称的接口进行连接,而名称不同则未连接
class Fetch extends Module{
    val io=IO(new Bundle{
        val instr=Output(UInt(32.W))
        val pc=Output(UInt(32.W))
    })
    //....取指实现....
}
class Decode extends Module{
    val io=IO(new Bundle{
        val instr=Input(UInt(32.W))
        val pc=Input(UInt(32.W))
        val aluOp=Output(UInt(5.W))
        val regA=Output(UInt(32.W))
        val regB=Output(UInt(32.W))
    })
    //....译码实现....
}
class Execute extends Module{
    val io=IO(new Bundle{
        val aluOp=Input(UInt(5.W))
        val regA=Input(UInt(32.W))
        val regB=Input(UInt(32.W))
        val result=Output(UInt(32.W))
    })
    //....执行实现....
}
class Top extends Module{
    val io=IO(new Bundle{
        //相应输入输出接口
    })
    val fetch=Module(new Fetch())
    val decode=Module(new Decode())
    val execute=Module(new Execute())
    //使用<>连接元件
    fetch.io<>decode.io
    decode.io<>execute.io
    io<>execute.io
}

//更简易的构造元件方法
//当你使用chisel的时候你要牢记你是在描述一个电路，作为比verilog更优秀的地方chisel内置了更简单的
//电路构造方法如接下来使用chisel的函数构造一个加法器
def adder(x:UInt,y:UInt)={
    x+y
}
val x=adder(a,b)//通过函数调用构造一个新的加法器
val y=adder(c,d)//另一个加法器
//函数作为轻量级硬件生成器也可以包括状态，当然你可以使用scala的各种语法来构造
def delay(x:UInt) = RegNext(x)
val delOut = delay(delay(delIn))//生成一个两个单位延迟的元件

//组合逻辑设计
//=操作符用于被预先已经声明的组合模块中会造成编译器错误
val e=(a&b)|c
val f=~e
e=c&b//WRONG！！！
//Chisel也支持根据条件更新电路，只要先声明一个Wire通过:=操作符实现更新
val w=Wire(UInt())
w:=0.U
when(cond){
    w:=3.U
}
//类似于高级程序设计语言的if-else，在HDL中Chisel也具有类似的操作
val w=Wire(UInt())
when(cond){
    w:=1.U
}.otherwise{
    w:=2.U
}//chisel-book Figure7.1
//同样也有if-else if-else 一样的操作
val w=Wire(UInt())
when(cond){
    w:=1.U
}.elsewhen{
    w:=2.U
}.otherwise{
    w:=3.U
}//复用器的描述,如果使用单一信号可以使用前面介绍的switch-is
//对于复杂的电路可以先赋值一个时间的值
val w=WireDefault(0.U)
when(cond){
    w:=3.U
}
//注：scala当中判断语句并没有生成硬件的作用

//译码器Decoder
import chisel.util._
//switch需要用到util包中的资源
result:=0.U//这个赋值不会被激活后端工具会优化掉，之所以赋值是为了避免组合电骡的非完全赋值
switch(sel){
        is(0.U){ result := 1.U }
        is(1.U){ result := 2.U }
        is(2.U){ result := 4.U }
        is(3.U){ result := 8.U }
    }
//更清晰的版本
switch(sel){
        is("b00".U){ result := "b0001".U }
        is("b01".U){ result := "b0010".U }
        is("b10".U){ result := "b0100".U }
        is("b11".U){ result := "b1000".U }
    }
//上面的操作多少是有点拖沓的，我们可以直接通过对sel信号的移位达到同样的效果
result := 1.U << sel
//编码器Encoder
b := "b00".U
switch(a){
        is("b0001".U){ b := "b00".U }
        is("b0010".U){ b := "b01".U }
        is("b0100".U){ b := "b10".U }
        is("b1000".U){ b := "b11".U }
    }
//小测试 4位输入显示至7段数码管
val cIn=Wire(UInt(4.W))
val cOut=Wire(UInt(7.W))
switch(a){
        is("b0000".U){ b := "b1111110".U }//a,b,c,d,e,f 0
        is("b0001".U){ b := "b0110000".U }//b,c 1
        is("b0010".U){ b := "b1101101".U }//a,b,d,e,g 2
        is("b0011".U){ b := "b1111001".U }//a,b,c,d,g 3
        is("b0100".U){ b := "b0110110".U }//b,c,f,g 4
        is("b0101".U){ b := "b1011011".U }//a,c,d,f,g 5
        is("b0110".U){ b := "b1011111".U }//a,c,d,e,f,g 6
        is("b0111".U){ b := "b1110001".U }//a,b,c 7
        is("b1000".U){ b := "b1111111".U }//a,b,c,d,e,f,g 8
        is("b1001".U){ b := "b1110011".U }//a,b,c,f,g 9
    }

//时序逻辑设计
//在Chisel中一个d输入和q输出的寄存器这样被定义，当然clock信号在Chisel内部间接完成不需要定义
val q=RegNext(d)
//当然也可以两步定义
val regDelay=Reg(UInt(4.W))
regDelay := delayIn
//Chisel中也可以自定义复位信号,与clock一样也是内部间接完成的
val valReg=RegInit(0.U(4.W))
valReg := inVal
//具有使能端的寄存器
val enableReg=Reg(UInt(4.W))
when(enable){
    enableReg:=inVal
}
//同样也可以为具有使能端的寄存器设置重置信号
val resetEnableReg=RegInit(0.U(4.W))
when(enable){
    resetEnableReg:=inVal
}
//寄存器也可以成为表达式的一部分，以下电路检测了信号的上升沿
val risingEdge=din&(!RegNext(din))

//计数器 chisel-book Figure 8.6
//长度为4（0-15循环）具有复位功能的计数器
val cntReg=RegInit(0.U(4.W))
cntReg := cntReg+1.U
//当我们想要计数时间的时候可以使用条件增加计数器
val cntEventsReg=RegInit(0.U(4.W))
when(event){
    cntEventsReg:=cntEventsReg+1.U
}
