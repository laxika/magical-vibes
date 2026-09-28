package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted creature's or planeswalker's controller chooses whether the source deals damage
 * to them or to the targeted permanent.
 */
public record DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect(
        int targetDamage, int controllerDamage, MayEffect controllerDamageFollowUp) implements CardEffect {

    public DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect(
            int targetDamage, int controllerDamage) {
        this(targetDamage, controllerDamage, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creatureOrPlaneswalker());
    }
}
