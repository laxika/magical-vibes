package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves a target Aura (already on the battlefield, attached to a creature) onto a target creature.
 * Used by cards like Crown of the Ages that can reposition Auras between creatures.
 * Reads targetIds[0] as the Aura and [1] as the creature to attach it to.
 */
public record AttachTargetAuraToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
