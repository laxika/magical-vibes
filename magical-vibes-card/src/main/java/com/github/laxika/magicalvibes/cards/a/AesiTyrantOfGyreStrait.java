package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "SLD", collectorNumber = "1442")
@CardRegistration(set = "SLD", collectorNumber = "1873")
public class AesiTyrantOfGyreStrait extends Card {

    public AesiTyrantOfGyreStrait() {
        // You may play an additional land on each of your turns.
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        // Whenever a land enters the battlefield under your control, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
