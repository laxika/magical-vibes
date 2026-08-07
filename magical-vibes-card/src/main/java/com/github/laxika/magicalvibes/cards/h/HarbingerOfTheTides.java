package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "58")
public class HarbingerOfTheTides extends Card {

    public HarbingerOfTheTides() {
        // "You may cast this spell as though it had flash if you pay {2} more to cast it."
        // Modelled as a flash-granting alternate hand cast of {U}{U} plus {2}.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}{U}")), null, true));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a tapped creature an opponent controls"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target tapped creature an opponent controls to its owner's hand?"));
    }
}
