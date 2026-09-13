package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/** Protects the permanent enchanted by this Aura by sacrificing the Aura and granting indestructible until end of turn. */
public record SacrificeAuraAndGrantIndestructibleEffect() implements DestructionReplacementEffect {

    @Override
    public DestructionReplacement replacement() {
        return DestructionReplacement.SACRIFICE_AURA_AND_GRANT_INDESTRUCTIBLE;
    }

    @Override
    public boolean appliesTo(Permanent source, Permanent destroyedPermanent) {
        return source.getAttachedTo() != null
                && source.getAttachedTo().equals(destroyedPermanent.getId());
    }
}
