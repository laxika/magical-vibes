package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "210")
public class Terrasymbiosis extends Card {

    public Terrasymbiosis() {
        addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn(),
                OncePerTurnTriggerEffect.markOnAcceptance(
                        new MayEffect(new DrawCardEffect(new EventValue()), "Draw that many cards?")));
    }
}
