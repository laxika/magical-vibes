package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOS", collectorNumber = "232")
public class StadiumTidalmage extends Card {

    public StadiumTidalmage() {
        // Whenever Stadium Tidalmage enters or attacks, you may draw a card. If you do, discard a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DrawAndDiscardCardEffect(), "Draw a card and discard a card?"));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DrawAndDiscardCardEffect(), "Draw a card and discard a card?"));
    }
}
