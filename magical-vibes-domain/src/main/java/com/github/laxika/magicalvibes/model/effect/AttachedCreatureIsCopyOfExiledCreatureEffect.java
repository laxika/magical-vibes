package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Marks the layer-1 copy effect created while an Equipment remains attached. */
public record AttachedCreatureIsCopyOfExiledCreatureEffect(Card copiedCard) implements CardEffect {

    public AttachedCreatureIsCopyOfExiledCreatureEffect() {
        this(null);
    }
}
