package com.jf.poi.render;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: admin
 * Date: 2018-12-11
 * Time: 18:53
 */
public class StateRender extends AbstractCellRender<String> {

    @Override
    public String render(String s) {
        if ("1".equals(s)) {
            return "冻结";
        }
        return "正常";
    }

}
