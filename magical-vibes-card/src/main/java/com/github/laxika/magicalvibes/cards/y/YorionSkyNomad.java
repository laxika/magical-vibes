package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "232")
public class YorionSkyNomad extends Card {

    public YorionSkyNomad() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                FlickerEffect.exileControllersAnyNumberPermanentsReturnAtStep(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentOwnedBySourceControllerPredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                        TurnStep.END_STEP, false));
    }
}
