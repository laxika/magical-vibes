package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;

@CardRegistration(set = "PLS", collectorNumber = "61")
public class GoblinGame extends Card {

    public GoblinGame() {
        addEffect(EffectSlot.SPELL, new GoblinGameEffect());
    }
}
