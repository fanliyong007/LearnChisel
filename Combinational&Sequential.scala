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
result:=0.U//这个赋值不会被激活后端工具会优化掉，之所以赋值是为了避免组合电路的非完全赋值
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
//向上向下计数
val cntReg=RegInit(0.U(8.W))
cntReg := cntReg + 1.U//每次给计数器加一
when(cntReg === N){
    cntReg := 0.U //到达指定数值复位
}
//也可以使用复用器来实现相同功能
val cntReg = RegInit(0.U(8.W))
cntReg := Mux(cntReg === N , 0.U , cntReg + 1.U)
//要时刻明白我们是在描述电路所以cntReg在脉冲信号下时刻在更新，语句不是执行的概念，我们是在描述一个客观元件
//倒计时计数器
val cntReg = RegInit(N)
cntReg := cntReg -1.U
when( cntReg === 0.U{
    cntReg := N
}

//当我们需要很多计数器时，用具有参数的函数去生成计数更为方便
//以下为scala定义函数的规范
// 用“def”开始函数定义
//        | 函数名
//        |   |  参数及参数类型
//        |   |        |   函数返回结果的类型
//        |   |        |          |  等号
//        |   |        |          |   |
//       def max(x: Int, y: Int): Int = {
//         if(x > y)
//           x
//         else  |
//           y   | 
//       }       |
//               |
//        花括号里定义函数体
def genCounter(n:Int) = {
    val cntReg = RegInit(0.U(8.W))
    cntReg := Mux(cntReg === n.U,0.U,cntReg+1.U)
    cntReg //return cntReg
}
//接下来我们可以很容易定义需要的计数器
val count10 = genCounter(10)
val count99 = genCounter(99)

//使用计数器产生时序chisel-book Figure 8.8
val tickCounterReg = RegInit(0.U(4.W))
val tick = tickCounterReg === (N-1).U
tickCounterReg := tickCounterReg + 1.U
when(tick){
    tickCounterReg := 0.U
}

//nerd计数器（当我们向下计数时容易把计数变成负数，这时候可以利用补码的特性只需要比较最高位即符号位是否为1就能知道）
val MAX = (N-2).S(8.W)
val cntReg = RegInit(MAX)
io.tick := false.B
cntReg := cntReg - 1.S
when(cntReg(7)){//判断符号位是否为1
    cntReg := MAX
    io.tick := true.B
}

//定时器 chisel-book Figure8.9
val cntReg = RegInit(0.U(8.W))
val done = cntReg === 0.U
val next = WireInit(0.U)
when(load){
    next := din
}.elsewhen(!done){
    next := cntReg - 1.U
}
cntReg := next

//脉冲宽度调制（PWM）发生器(占空比是指高电平在一个周期内所占的百分比)
def pwm(nrCycles:Int,din:UInt)={
    val cntReg = RegInit(0.U(unsignedBitLength(nrCycles-1).W))
    //unsignedBitLength函数用于规定计数器cntReg的上限
    cntReg := Mux(cntReg===(nrCycles-1).U,0.U,cntReg+1.U)
    din>cntReg // return din>cntReg
}
val din=3.U
val dout=pwm(10,din)//创建一个每10个时钟周期出现3个高电平时钟周期的波形
//更复杂的一个例子
val FREQ = 100000000 //一个100MHz的时钟输入
val MAX = FREQ/1000 // 1kHz
val modulationReg = RegInit(0.U(32.W))
val upReg = RegInit(true.B)//向上计数或向下计数的标记
when(modulationReg < FREQ.U && upReg){
    modulationReg := modulationReg + 1.U
}.elsewhen(modulationReg === FREQ.U && upReg){
    upReg := false.B
}.elsewhen(modulationReg > FREQ.U && !upReg){
    modulationReg := modulationReg - 1.U
}.otherwise{
    upReg := true.B
}
val sig = pwm(MAX,modulationReg >> 10) //移位操作消耗的硬件资源比除法来的少很多，用2的10次代替1000也不是不可以

//移位寄存器 chisel-book Figure8.11
val shiftReg = Reg(UInt(4.W))//创建一个4为移位寄存器
shiftReg := Cat(shiftReg(2,0),din)//将移位寄存器的低三位到输入din用于下一个寄存器的输入
val dout = shiftReg(3)//将最高的寄存器用于输出dout
//移位寄存器常用来将串行数据转换为并行数据
//将串行输入转换为并行输出的移位寄存器chisel-book Figure 8.12
val outReg = RegInit(0.U(4.W))
outReg := Cat(serIn,outReg(3,1))
val q = outReg
//并行读取的移位寄存器chisel-book Figure 8.13
when(load){
    loadReg := d
}.otherwise{
    loadReg := Cat(0.U,loadReg(3,1))
}
val serOut = loadReg(0)

//存储器
class Memory() extends Module{
    val io = IO(new Bundle{
        val rdAddr = Input(UInt(10.W))
        val rdData = Output(UInt(8.W))
        val wrEna = Input(Bool())
        val wrData = Input(UInt(8.W))
        val wrAddr = Input(UInt(10.W))
    })
    val mem = SyncReadMen(1024,UInt(8.W))//Chisel提供存储器构建器,1024代表单位数量，第二个参数代表单位大小
    io.rdData := mem.read(io.rdAddr)
    when(io.wrEna){
        men.write(io.wrAddr,io.wrData)
    }
}
//如果想要读出新写入的值，我们可以搭建一个前级电路能够检查出相同地址情况下前面路径
class ForwardingMemory() extends Module{
    val io = IO(new Bundle{
        val rdAddr = Input(UInt(10.W))
        val rdData = Output(UInt(8.W))
        val wrEna = Input(Bool())
        val wrData = Input(UInt(8.W))
        val wrAddr = Input(UInt(10.W))
    })
    val mem = SyncReadMen(1024,UInt(8.W))
    val wrDataReg = RegNext(io.wrData)
    val doForwardReg = RegNext(io.wrAddr === io.rdAddr && io.wrEna)
    val memData = mem.read(io.rdAddr)
    io.rdData := mem.read(io.rdAddr)
    when(io.wrEna){
        men.write(io.wrAddr,io.wrData)
    }
    io.rdData := Mux(doForwardReg,wrDataReg,memData)
}