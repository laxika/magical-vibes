package com.github.laxika.magicalvibes.model.effect;

/**
 * A death trigger that returns the creature card that died (the stack entry's triggering card) to its owner's hand
 * unless its targeted player pays {@code lifeCost} life (Athreos, God of Passage).
 *
 * @param lifeCost how much life the targeted player may pay
 */
public record ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(int lifeCost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
