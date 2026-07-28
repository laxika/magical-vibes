package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher removal: destroy the target creature unless its controller pays life equal to that
 * creature's toughness; a creature destroyed this way can't be regenerated. The decision belongs to
 * the target creature's controller — a controller who can pay is prompted via the may-ability
 * system; one who can't (too little life, or life can't change) has the creature destroyed
 * immediately. The life cost is read from the creature's toughness as the spell resolves.
 * Used by Essence Vortex.
 */
public record DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
