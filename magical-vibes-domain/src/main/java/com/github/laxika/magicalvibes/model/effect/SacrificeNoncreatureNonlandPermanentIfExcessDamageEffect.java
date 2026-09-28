package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Marker for Maarika's self-scoped excess-damage trigger. The damaged creature's controller
 * sacrifices a noncreature, nonland permanent when the trigger resolves.
 */
public record SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect()
        implements DamagedCreatureTriggerEffect {

    @Override
    public CardEffect triggeredEffect() {
        return new SacrificePermanentsEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                        new PermanentNotPredicate(new PermanentIsLandPredicate()))),
                SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER);
    }

    @Override
    public boolean requiresExcessDamage() {
        return true;
    }

    @Override
    public boolean useDamagedCreatureAsTriggeringPermanent() {
        return true;
    }
}
