package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "That creature fights target creature" — the creature whose entry triggered the ability fights the
 * chosen target (CR 701.14a). The fighter is the stack entry's {@code triggeringPermanentId}, falling
 * back to the source permanent when the source itself is the creature that entered. Restrict the
 * target with the card's own {@code TargetFilter} (Gruul Ragebeast: a creature an opponent controls).
 *
 * <p>With {@code noFight} the two creatures instead just deal damage equal to their power to each other
 * without fighting (Stalking Yeti), so "fought this turn" checks are not enabled.
 */
public record EnteringCreatureFightsTargetCreatureEffect(PermanentPredicate targetPredicate, boolean optionalTarget,
                                                         boolean noFight) implements CardEffect {

    public EnteringCreatureFightsTargetCreatureEffect(PermanentPredicate targetPredicate, boolean optionalTarget) {
        this(targetPredicate, optionalTarget, false);
    }

    public EnteringCreatureFightsTargetCreatureEffect(PermanentPredicate targetPredicate) {
        this(targetPredicate, false);
    }

    /** "It deals damage equal to its power to target creature and that creature deals damage equal to its power to it." */
    public static EnteringCreatureFightsTargetCreatureEffect dealingPowerDamageToEachOther() {
        return new EnteringCreatureFightsTargetCreatureEffect(null, false, true);
    }

    @Override
    public boolean hasOptionalTarget() {
        return optionalTarget;
    }

    public EnteringCreatureFightsTargetCreatureEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), targetPredicate);
    }
}
