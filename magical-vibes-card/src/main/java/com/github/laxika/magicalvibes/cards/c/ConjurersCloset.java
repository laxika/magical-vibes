package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "214")
@CardRegistration(set = "INR", collectorNumber = "259")
@CardRegistration(set = "INR", collectorNumber = "321")
@CardRegistration(set = "INR", collectorNumber = "441")
public class ConjurersCloset extends Card {

    public ConjurersCloset() {
        // At the beginning of your end step, you may exile target creature you control,
        // then return that card to the battlefield under your control.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(FlickerEffect.flickerTargetUnderYourControl(),
                        "Exile target creature you control and return it?"));
    }
}
