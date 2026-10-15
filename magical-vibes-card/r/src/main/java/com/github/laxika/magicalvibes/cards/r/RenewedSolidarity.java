package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffect;

@CardRegistration(set = "DRC", collectorNumber = "7")
@CardRegistration(set = "DRC", collectorNumber = "23")
public class RenewedSolidarity extends Card {

    public RenewedSolidarity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 0));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffect());
    }
}
