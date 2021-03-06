package ui.anwesome.com.tictacboxview

/**
 * Created by anweshmishra on 22/03/18.
 */
import android.app.Activity
import android.view.*
import android.content.*
import android.graphics.*
class TicTacBoxView(ctx : Context) : View(ctx) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val renderer : Renderer = Renderer(this)
    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }
    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }
    data class State(var prevScale : Float = 0f, var j : Int = 0, var dir : Int = 0) {
        val scales : Array<Float> = arrayOf(0f, 0f, 0f, 0f)
        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir
                if (j == scales.size || j == -1) {
                    j -= dir
                    dir = 0
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }
        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0) {
                dir = 1 - 2 * prevScale.toInt()
                startcb()
            }
        }
    }
    data class Animator(var view : View, var animated : Boolean = false) {
        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }
        fun stop() {
            if (animated) {
                animated = false
            }
        }
        fun animate(updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                }
                catch (ex : Exception) {

                }
            }
        }
    }
    data class TicTacBox(var i : Int, val state : State = State()) {
        fun draw(canvas : Canvas, paint : Paint) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val size = Math.min(w,h)/3
            paint.color = Color.WHITE
            paint.strokeWidth = size/20
            paint.strokeCap = Paint.Cap.ROUND
            canvas.save()
            canvas.translate(w/2, h/2)
            for (i in 0..1) {
                canvas.save()
                canvas.rotate(90f * i * state.scales[0])
                for (j in 0..1) {
                    canvas.save()
                    canvas.translate(size/6 * (1 - 2 * j) * state.scales[1], -size/2)
                    canvas.drawLine(0f, 0f, 0f, size, paint)
                    canvas.restore()
                }
                canvas.restore()
            }
            canvas.save()
            canvas.translate(-size/3 , -size/3)
            for (i in 0..2) {
                for (j in 0..2) {
                    canvas.save()
                    canvas.translate((size/3) * i, (size/3) * j)
                    canvas.scale(state.scales[2], state.scales[2])
                    if ((i +j) % 2 == 1) {
                        canvas.drawLine(-size/12, -size/12, size/12, size/12, paint)
                        canvas.drawLine(size/12, -size/12, -size/12, size/12, paint)
                    }
                    else {
                        paint.style = Paint.Style.STROKE
                        canvas.drawCircle(0f, 0f, size/12, paint)
                    }
                    canvas.restore()
                }
            }
            canvas.restore()
            for(i in 0..1) {
                canvas.save()
                canvas.rotate(90f * i + 45f)
                val lineH =  2 * (size / 2 + size/6) * state.scales[3]
                val initY = -size/2 - size/6
                canvas.drawLine(0f, initY, 0f, initY + lineH, paint)
                canvas.restore()
            }
            canvas.restore()
        }
        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }
        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }
    }
    data class Renderer(var view : TicTacBoxView) {
        val ticTacBox : TicTacBox = TicTacBox(0)
        val animator : Animator = Animator(view)
        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            ticTacBox.draw(canvas, paint)
            animator.animate {
                ticTacBox.update {
                    animator.stop()
                }
            }
        }
        fun handleTap() {
            ticTacBox.startUpdating {
                animator.start()
            }
        }
    }
    companion object {
        fun create(activity : Activity) : TicTacBoxView {
            val view = TicTacBoxView(activity)
            activity.setContentView(view)
            return view
        }
    }
}