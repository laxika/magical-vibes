package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PlanarDieRoller {
    public PlanarDieResult roll() {
        return switch (ThreadLocalRandom.current().nextInt(6)) {
            case 4 -> PlanarDieResult.CHAOS;
            case 5 -> PlanarDieResult.PLANESWALKER;
            default -> PlanarDieResult.BLANK;
        };
    }
}
