package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "RNA", collectorNumber = "55")
public class SphinxOfForesight extends Card {

    public SphinxOfForesight() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new ScryEffect(3),
                "Reveal this card from your opening hand?"
        ));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ScryEffect(1));
    }
}
