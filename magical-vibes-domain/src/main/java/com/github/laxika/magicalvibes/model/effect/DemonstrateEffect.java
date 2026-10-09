package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/**
 * Copies the spell that caused this resolving cast trigger for its controller, then lets that
 * player choose an opponent who also gets a copy.
 */
public record DemonstrateEffect(StackEntry spellSnapshot) implements TriggeringSpellReferencingEffect {

    public DemonstrateEffect() {
        this(null);
    }

    /** Returns whether an effect already contains a demonstrate trigger. */
    public static boolean isWrappedBy(CardEffect effect) {
        return effect instanceof DemonstrateEffect
                || effect instanceof MayEffect may && isWrappedBy(may.wrapped());
    }
}
