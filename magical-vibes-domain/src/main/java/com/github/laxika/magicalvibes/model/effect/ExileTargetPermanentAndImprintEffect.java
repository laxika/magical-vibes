package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a target permanent and imprints the exiled card onto the source permanent.
 * The exile is permanent (NOT until-source-leaves). Used by Exclusion Ritual.
 */
public record ExileTargetPermanentAndImprintEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
