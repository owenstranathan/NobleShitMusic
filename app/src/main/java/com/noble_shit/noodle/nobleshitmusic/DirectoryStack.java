package com.noble_shit.noodle.nobleshitmusic;

import java.util.Stack;

/**
 * Created by Noodle on 8/11/17.
 */
public class DirectoryStack{
    private String path_;
    private Stack<String> stack_;

    public DirectoryStack(String path) {
        this.path_ = path;
        this.stack_ = new Stack<String>();
        this.buildStack(path.split("/"));
    }

    private void buildStack(String [] pathList) {
        for(String dir : pathList) {
            this.stack_.push(dir);
        }
    }

    public String getPath() {
        this.buildPath();
        return this.path_;
    }

    public String getArrowPath() {
        String arrowPath = "";
        for(String s : this.stack_) {
            arrowPath += s + " > ";
        }
        return arrowPath;
    }

    public int size() {
        return this.stack_.size();
    }

    public String peek() {
        return stack_.peek();
    }

    public String peek(int i){
        int lastIndex = this.stack_.size() - 1;
        if (i > lastIndex)
            throw new IndexOutOfBoundsException();
        return this.stack_.get(lastIndex-i);
    }

    public void pop() {
        this.stack_.pop();
    }

    public boolean empty() {
        return this.stack_.empty();
    }

    public void push(String item) {
        this.stack_.push(item);
    }

    private void buildPath() {
        this.path_ = "/";
        for(String s : this.stack_) {
            this.path_ += s + "/";
        }

    }

}
