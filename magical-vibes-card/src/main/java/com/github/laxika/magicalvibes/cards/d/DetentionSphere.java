package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "155")
public class DetentionSphere extends Card {

    public DetentionSphere() {
        // When this enchantment enters, you may exile target nonland permanent not named
        // Detention Sphere and all other permanents with the same name as that permanent.
        // When this enchantment leaves the battlefield, return the exiled cards to the
        // battlefield under their owner's control (handled by the effect).
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentNamedPredicate("Detention Sphere"))
                )),
                "Target must be a nonland permanent not named Detention Sphere"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(
                                new ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect(),
                                "Exile target nonland permanent and all with the same name?"));
    }
}
