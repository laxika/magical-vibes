package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/** Protects the permanent enchanted by this Aura by destroying the Aura instead. */
public record TotemArmorEffect() implements DestructionReplacementEffect {

    @Override
    public DestructionReplacement replacement() {
        return DestructionReplacement.UMBRA_ARMOR;
    }

    @Override
    public boolean appliesTo(Permanent source, Permanent destroyedPermanent) {
        return source.getAttachedTo() != null
                && source.getAttachedTo().equals(destroyedPermanent.getId());
    }
}
