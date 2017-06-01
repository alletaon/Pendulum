package com.karev.pendulum;

import org.junit.Test;

import static org.junit.Assert.*;

public class ControllerTest {
    Controller con = new Controller();
    @Test
    public void calcCompens() throws Exception {
        assertEquals(-15, con.calcCompens(17));
        assertEquals(15, con.calcCompens(-17));
        assertEquals(12, con.calcCompens(12));
        assertEquals(-12, con.calcCompens(-12));
        assertEquals(-6, con.calcCompens(58));
        assertEquals(6, con.calcCompens(-58));
        assertEquals(0, con.calcCompens(0));
    }

}