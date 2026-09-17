package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/** Produces random results for ordinary dice. */
@Component
public class DiceRollService {

    public int roll(int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("A die must have at least one side");
        }
        return ThreadLocalRandom.current().nextInt(sides) + 1;
    }
}
