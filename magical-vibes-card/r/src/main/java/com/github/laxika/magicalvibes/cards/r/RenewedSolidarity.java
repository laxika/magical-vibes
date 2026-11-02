package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRC", collectorNumber = "7")
@CardRegistration(set = "DRC", collectorNumber = "23")
public class RenewedSolidarity extends Card {

    public RenewedSolidarity() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new BoostCreaturesOfChosenSubtypeEffect(1, 0));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffect());
    }
}
