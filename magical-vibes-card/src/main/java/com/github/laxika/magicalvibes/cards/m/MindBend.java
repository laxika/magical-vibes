package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "93")
@CardRegistration(set = "9ED", collectorNumber = "87")
@CardRegistration(set = "8ED", collectorNumber = "92")
public class MindBend extends Card {

    public MindBend() {
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, true, false));
    }
}
