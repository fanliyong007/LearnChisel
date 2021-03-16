//blink led
class Hello extends Module{
    val io=IO(new Bundle{
        val led=Output(UInt(1.w))
    })
    val CNT_MAX=(50000000/2-1).U//50MHz计数

    val cntReg=RegInit(0.U(32.W))//计数寄存器
    val blkReg=RegInit(0.U(1.W))//LED状态寄存器

    cntReg := cntReg+1.U
    when(cntReg===CNT_MAX){
        cntReg:=0.U//复位
        blkReg:=~blkReg//反转LED状态寄存器
    }
    io.led:=blkReg//更改LED状态
}

Bits(n.W)//表示Bits类型的n位数据
Bits(8.W)
UInt(8.W)//UInt继承Bits 无符号整型
SInt(8.W)//SInt同样继承Bits 有符号整型使用补码表示
"hff".U//h表示16进制ff表示数字大小U表示类型
"o377".U//o表示8进制
"b1111_1111".U//b表示二进制，下划线是标识数字
Bool()
true.B
false.B//布尔类型

//组合逻辑电路
val logic= a & b | c 
val and=a&b//bitwise and 与运算
val or=a|b//bitwise or 或运算
val xor=a^b//bitwise xor 异或运算
val not=~a//bitwise negation 非运算

val add=a+b//addition 加法运算
val sub=a-b//subtraction 减法运算
val neg=-a//negate 相反数
val mul=a*b//multiplication 乘法运算
val div=a/b//divsion 除法运算
val mod=a%b//modulo operation除法运算

val w=Wire(UInt())
w:=a&b//可以先定义位某种类型的wire然后赋值，赋值操作使用:=

val sign=x(31)//类似于数组的读取操作，对一个值的某位也可以使用()访问
val lowByte=largeWord(7,0)//()操作符同样实现一个分割从终点到起点提取
val word=Cat(highByte,lowByte)//Cat可以合并两个bit

//复用器(单路选择器)
val result=Mux(sel,a,b)
//根据sel的信号选择，当sel位true.B时候选择a否则选择b，a与b可以是任何Chisel类型或者集合只要a与b类型相同

//状态寄存器（由D触发器构成）
val reg=RegInit(0.U(8.W))//8位置零寄存器 上升沿触发
reg:=signIn
val signOut=reg
//一个输入连接到寄存器，通过:=更新操作数，输出的寄存器可以使用表达式通过名字调用
val regNxt=RegNext(signIn)//也可以使用这种方式连接到它的输入
val bothReg=RegNext(signIn,0.U)//还可以这样连接到它的输入并使用一个常量作为初始值作为定义

//简单计数器
val cntReg=RegInit(0.U(8.W))
cntReg:=Mux(cntReg===100.U,0.U,cntReg+1.U)

//Bundle与Vec
//Bundle有点类似于C或C++等类似语言中的结构体或类，而Vec更像vector或是数组
val ch=Wire(new Channel())//通过new来将Channel包裹进Wire
ch.data:=123.U
ch.valid:=true.B
val b=ch.valid//通过点来访问也同样有点类似与C++或Java中的对象和对象成员
val channel=ch//可以整个赋值

val v=Vec(8,0.U(8.W))
val idx=1.U(2.W)
val a=v(idx)//类似于数组Vec也可用通过()操作符访问
val registerFile=Reg(Vec(32,UInt(32.W)))//将向量传入寄存器去定义一列寄存器
registerFile(idx):=dIn
val dOut=registerFile(idx)//可以通过索引访问寄存器的一个元素

//可随意混搭Vec与Bundle
val vecBundle=Wire(Vec(8,new Channel()))
class BundleVec extends Bundle{
    val field=UInt(8.W)
    val vector=Vec(4.UInt(8.W))
}
//当我们需要一个置0寄存器的时候可以先创造一个具有Bundle的Wire然后设置单独的域，把Bundle传给RegInit
val initVal=Wire(new Chhannel())
initVal.data:=0.U
initVal.valid:=false.B
val channelReg=RegInit(initVal)