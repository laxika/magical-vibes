package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MRD", collectorNumber = "46")
public class PsychicMembrane extends Card {

    public PsychicMembrane() {
        addEffect(EffectSlot.ON_BLOCK, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
