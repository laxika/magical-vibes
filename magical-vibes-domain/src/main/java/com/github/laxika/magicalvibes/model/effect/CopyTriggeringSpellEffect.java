package com.github.laxika.magicalvibes.model.effect;

/** Creates a copy of the spell that caused the resolving spell-cast trigger. */
public record CopyTriggeringSpellEffect(boolean tokenCopy) implements TriggeringSpellReferencingEffect {

    public CopyTriggeringSpellEffect() {
        this(false);
    }
}
