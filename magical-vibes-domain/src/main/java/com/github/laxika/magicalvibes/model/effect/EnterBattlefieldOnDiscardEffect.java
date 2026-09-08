package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

public record EnterBattlefieldOnDiscardEffect(CounterType counterType, int counterCount) implements CardEffect {

    public EnterBattlefieldOnDiscardEffect() {
        this(null, 0);
    }
}
