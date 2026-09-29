package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardCardsEffect;

@CardRegistration(set = "ROE", collectorNumber = "128")
@CardRegistration(set = "M3C", collectorNumber = "205")
public class SufferThePast extends Card {

    public SufferThePast() {
        addEffect(EffectSlot.SPELL, new ExileTargetPlayerGraveyardCardsEffect(1, 1));
    }
}
