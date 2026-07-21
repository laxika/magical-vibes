package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Controller scries {@code count} — look at the top N cards, put any number on the bottom in any
 * order, rest on top in any order (CR 701.22). Amount may be fixed ("scry 2") or dynamic
 * ("scry X, where X is the number of Zombies you control").
 */
public record ScryEffect(DynamicAmount count) implements CardEffect {

    public ScryEffect(int count) {
        this(new Fixed(count));
    }
}
