package com.utd.distributed.util;

import java.util.ArrayList;
import java.util.List;

public class Message {
	public List<Integer> val;
    public List<Integer> level;
    public int key;

    public Message(int key, ArrayList<Integer> val, ArrayList<Integer> level) {

        this.val = val;
        this.key = key;
        this.level = level;
    }
}
