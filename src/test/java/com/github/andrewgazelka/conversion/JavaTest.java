package com.github.andrewgazelka.conversion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JavaTest {

    @Test
    void testUnit(){

        Meter meters = Units.ft(20);
        Assertions.assertEquals(6.096, meters.getValue(), 1E-6);

        Velocity div = meters.div(Units.sec(2.0)); // 20 ft per 2.0 second (in meters / second)
        // should be about 3.048
        Assertions.assertEquals(3.048, div.getValue(), 1E-6);
    }
}
