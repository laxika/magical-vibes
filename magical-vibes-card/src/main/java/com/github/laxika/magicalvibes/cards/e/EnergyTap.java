package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "69")
public class EnergyTap extends Card {

    public EnergyTap() {
        // Tap target untapped creature you control. If you do, add an amount of {C} equal to that
        // creature's mana value. Single target (untapped creature you control): if it is no longer a
        // legal target at resolution the spell fizzles and neither effect happens, satisfying "If you do".
        // Same target group; the tap runs first, then {C} equal to the still-present creature's mana value.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate())
                )),
                "Target must be an untapped creature you control"
        ))
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.COLORLESS, new TargetManaValue()));
    }
}
