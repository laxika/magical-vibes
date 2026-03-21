package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DOM", collectorNumber = "206")
public class TatyovaBenthicDruid extends Card {

    public TatyovaBenthicDruid() {
        // Landfall — Whenever a land you control enters, you gain 1 life and draw a card.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new DrawCardEffect());
    }
}
