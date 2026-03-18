package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile target creature and all other creatures with the same name as that creature.
 * Used by Sever the Bloodline.
 */
public record ExileTargetCreatureAndAllWithSameNameEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
