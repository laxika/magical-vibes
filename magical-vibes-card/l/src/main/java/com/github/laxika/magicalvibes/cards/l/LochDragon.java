package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ELD", collectorNumber = "211")
public class LochDragon extends Card {

    public LochDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));
    }
}
