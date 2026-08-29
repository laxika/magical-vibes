package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Static replacement effect: the controller's unspent mana becomes the chosen color when it would
 * drain. The no-argument form preserves the original colorless replacement used by Kruphix.
 */
public record ReplaceManaDrainWithColorlessEffect(ManaColor replacementColor) implements CardEffect {

    public ReplaceManaDrainWithColorlessEffect() {
        this(ManaColor.COLORLESS);
    }
}
