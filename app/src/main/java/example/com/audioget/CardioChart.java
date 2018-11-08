package example.com.audioget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CardioChart extends Fragment {

    private static String TAG = CardioChart.class.getSimpleName();
    private static final int MAX_POINT = 8000;
    private Handler handler;
    private String title = "实时波形";
    private XYSeries series;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
    XYSeriesRenderer r = new XYSeriesRenderer();
    private Context context;
    private int addX = -1;
    int[] xv = new int[MAX_POINT];
    int[] yv = new int[MAX_POINT];
    RelativeLayout breathWave;
    int i = 0;
    private Map<String, Integer> map1;
    Random random = new Random();
    private final int POINT_GENERATE_PERIOD=1; //单位是ms
    private double [] pa = new double[2000];
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            ArrayList<Integer> datas = new ArrayList<Integer>();
//            for (int i = 0; i < 1; i++) {
//                datas.add(random.nextInt(3000));
//            }
            /*以下方式左右拖动坐标系时可以看到所有历史数据点*/
            //updateCharts(datas);
            //leftUpdateCharts(datas);
            /*以下方式无法看到历史数据点，在坐标轴上只能看到MAX_POINT个数据点*/
            //updateChart(random.nextInt(3000));
            rightUpdateChart(pa);
            //handler.postDelayed(this, POINT_GENERATE_PERIOD);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map1 = new HashMap<String, Integer>();
        map1.put("START", 1);
        map1.put("STOP", 2);
        handler = new Handler() ;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cardiochart, container, false);
        breathWave = (RelativeLayout) view.findViewById(R.id.cardiograph);
        initCardiograph();
        return view;
    }
    /**
     * @Title revcievedMessage
     * @Description Fragment接收Activity中消息的第一种方式，直接在Fragment中提供一个public方法，供Activity调用
     * @param action
     * @return void
     */
    public void recievedMessage(String action, double [] para){
        switch(map1.get(action)){
            case 1 :
                pa = para;
                handler.postDelayed(runnable, POINT_GENERATE_PERIOD);
                //改成数据形式
                //handler.post(runnable);
                //pa = para;
                break;
            case 2 :
                Log.w(TAG, "recieved Stop !");
                handler.removeCallbacksAndMessages(null);
                break;
        }
    }

    @Override
    public void onResume() {
        //handler.postDelayed(runnable, POINT_GENERATE_PERIOD);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initCardiograph() {
        context = getActivity().getApplicationContext();
        // 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        series = new XYSeries(title);
        // 创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();
        // 将点集添加到这个数据集中
        mDataset.addSeries(series);
        // 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        /* int color = Color.parseColor("#08145e"); */
        //int color = getResources().getColor(R.color.cardio_color3);
        PointStyle style = PointStyle.CIRCLE;
        buildRenderer(Color.GREEN, style, true);
        // 设置好图表的样式
        setChartSettings(renderer, "X", "Y", 0, MAX_POINT, -100, 100, Color.GRAY, Color.GRAY);
        // 生成图表
        chart = ChartFactory.getCubeLineChartView(context, mDataset, renderer,0.3f);
        chart.setBackgroundColor(Color.TRANSPARENT);
        breathWave.removeAllViews();
        breathWave.addView(chart);
    }
    protected void buildRenderer(int color, PointStyle style, boolean fill) {
        // 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(1);
        renderer.addSeriesRenderer(r);
    }
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        // 有关对图表的渲染可参看api文档
        //renderer.setBackgroundColor(Color.TRANSPARENT);
        //renderer.setApplyBackgroundColor(true);
        //getResources().getColor(R.color.cardio_bg_color)
        renderer.setChartTitle(title);
        renderer.setChartTitleTextSize(16);
        //renderer.setLabelsTextSize(10);// 设置坐标轴标签文字的大小
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        //renderer.setYAxisAlign(Align.RIGHT, 0);//用来调整Y轴放置的位置，表示将第一条Y轴放在右侧
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        //renderer.setShowGrid(true);
        renderer.setShowGridX(true);
        renderer.setShowGridY(false);
        renderer.setGridColor(Color.GRAY);
        renderer.setXLabels(10);//若不想显示X标签刻度，设置为0 即可
        renderer.setYLabels(10);
        renderer.setLabelsTextSize(12);// 设置坐标轴标签文字的大小
        renderer.setXLabelsColor(labelsColor);
        renderer.setYLabelsColor(0, labelsColor);
        renderer.setYLabelsVerticalPadding(-5);
        renderer.setXTitle("");
        renderer.setYTitle("");
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setAxisTitleTextSize(20);
        renderer.setPointSize((float) 1);
        renderer.setShowLegend(false);
        renderer.setFitLegend(true);
        renderer.setMargins(new int[] { 30, 45, 0, 20 });// 设置图表的外边框(上/左/下/右)
        //renderer.setMargins(new int[] { 0, 0, 0, 0 });// 设置图表的外边框(上/左/下/右)
        renderer.setMarginsColor(Color.parseColor("#F0F0F0"));
        //renderer.setBackgroundColor(Color.WHITE);
    }
    /**
     * @Title leftUpdateCharts
     * @Description 新生成的点一直在左侧，产生向右平移的效果， 基于X轴坐标从0开始，然后递减的思想处理
     * @param datas
     * @return void
     */
    protected void  leftUpdateCharts(ArrayList<Integer> datas) {
        for (int addY : datas) {
            series.add(i, addY);
            i--;
        }
        if (Math.abs(i) < MAX_POINT) {
            renderer.setXAxisMin(-MAX_POINT);
            renderer.setXAxisMax(0);
        } else {
            renderer.setXAxisMin(-series.getItemCount());
            renderer.setXAxisMax(-series.getItemCount() + MAX_POINT);
        }

        chart.repaint();
    }

    /**
     * @Title updateCharts
     * @Description 新生成的点一直在右侧，产生向左平移的效果，基于X轴坐标从0开始，然后递加的思想处理
     * @param datas
     * @return void
     */
    protected void updateCharts(ArrayList<Integer> datas) {
        for (int addY : datas) {
            series.add(i, addY);
            i++;
        }
        if (i < MAX_POINT) {
            renderer.setXAxisMin(0);
            renderer.setXAxisMax(MAX_POINT);
        } else {
            renderer.setXAxisMin(series.getItemCount() - MAX_POINT);
            renderer.setXAxisMax(series.getItemCount());
        }

        chart.repaint();
    }

    /**
     * @Title updateChart
     * @Description 新生成的点一直在x坐标为0处，因为将所有旧点的x坐标值加1，所以产生向右平移的效果
     * @param addY
     * @return void
     */
    private void updateChart(int addY) {

        // 设置好下一个需要增加的节点
        addX = 0;
        // 移除数据集中旧的点集
        mDataset.removeSeries(series);
        // 判断当前点集中到底有多少点，因为屏幕总共只能容纳MAX_POINT个，所以当点数超过MAX_POINT时，长度永远是MAX_POINT
        int length = series.getItemCount();
        if (length > MAX_POINT) {
            length = MAX_POINT;
        }
        // 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (int i = 0; i < length; i++) {
            xv[i] = (int) series.getX(i) + 1;
            yv[i] = (int) series.getY(i);
        }
        // 点集先清空，为了做成新的点集而准备
        series.clear();
        // 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        // 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        series.add(addX, addY);
        for (int k = 0; k < length; k++) {
            series.add(xv[k], yv[k]);
        }
        // 在数据集中添加新的点集
        mDataset.addSeries(series);
        // 视图更新，没有这一步，曲线不会呈现动态
        // 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        //chart.invalidate();
        chart.repaint();
    }

    /**
     * @Title rightUpdateChart
     * @Description 新生成的点一直在x坐标为MAX_POINT处，因为将所有旧点的x坐标值减1，所以产生向左平移的效果，无法看到历史数据点
     * @param addY
     * @return void
     */
    //批量数据更新
    private void rightUpdateChart(double [] addY) {
        // 设置好下一个需要增加的节点
        int [] addArr = new int[addY.length] ;
        for(int i = 0;i<addY.length;i++)
            addArr[i] = MAX_POINT-addY.length+i+1;
        // 移除数据集中旧的点集
        mDataset.removeSeries(series);
        // 判断当前点集中到底有多少点，因为屏幕总共只能容纳MAX_POINT个，所以当点数超过MAX_POINT时，长度永远是MAX_POINT
        int length = series.getItemCount();
        if (length > MAX_POINT) {
            length = MAX_POINT;
        }
        // 将旧的点集中x和y的数值取出来放入backup中，并且将x的值-1，造成曲线向左平移的效果
        for (int i = 0; i < length; i++) {
            xv[i] = (int) series.getX(i) - addY.length;
            yv[i] = (int) series.getY(i);
        }
        // 点集先清空，为了做成新的点集而准备
        series.clear();
        // 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        // 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        for(int l = 0; l < addY.length; l++) {
            series.add(addArr[l], addY[l]);
        }
        for (int k = 0; k < length; k++) {
            series.add(xv[k], yv[k]);
        }

        // 在数据集中添加新的点集
        mDataset.addSeries(series);
        // 视图更新，没有这一步，曲线不会呈现动态
        // 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        //chart.invalidate();
        chart.repaint();
    }
}