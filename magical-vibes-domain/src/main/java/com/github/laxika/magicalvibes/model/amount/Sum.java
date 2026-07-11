package com.github.laxika.magicalvibes.model.amount;

import java.util.List;

/**
 * The sum of the component amounts. Components are counted independently, so a
 * permanent matching two summed {@link PermanentCount}s counts twice (e.g. War
 * Report counts each artifact creature once as a creature and once as an artifact).
 */
public record Sum(List<DynamicAmount> amounts) implements DynamicAmount {

    public Sum(DynamicAmount... amounts) {
        this(List.of(amounts));
    }
}
