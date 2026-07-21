package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "38")
public class RestorationAngel extends Card {

    public RestorationAngel() {
        // When this creature enters, you may exile target non-Angel creature you control,
        // then return that card to the battlefield under your control.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ANGEL))
                )),
                "Target must be a non-Angel creature you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(FlickerEffect.flickerTargetUnderYourControl(),
                        "Exile target non-Angel creature you control?"));
    }
}
