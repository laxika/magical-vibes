package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/** Mills each opponent, then returns every milled creature as a face-down Cyberman. */
public record MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect(DynamicAmount count)
        implements CardEffect {

    public MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect {
        Objects.requireNonNull(count, "count");
    }

    public MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect(int count) {
        this(new Fixed(count));
    }
}
