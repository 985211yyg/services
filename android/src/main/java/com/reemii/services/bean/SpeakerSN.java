package com.reemii.services.bean;

/**
 * Author: yyg
 * Date: 2020/3/9 11:27
 * Description:
 */
public class SpeakerSN {

    /**
     * code : 0
     * msg :
     * data : {"result":"7e8a33fa-63bd80f4-022a-006f-2178d"}
     */

    private int code;
    private String msg;
    private DataBean data;

    @Override
    public String toString() {
        return "SpeakerSN{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * result : 7e8a33fa-63bd80f4-022a-006f-2178d
         */

        private String result;

        @Override
        public String toString() {
            return "DataBean{" +
                    "result='" + result + '\'' +
                    '}';
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
