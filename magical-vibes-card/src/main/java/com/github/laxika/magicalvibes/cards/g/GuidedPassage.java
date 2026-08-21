package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GuidedPassageEffect;

@CardRegistration(set = "APC", collectorNumber = "105")
public class GuidedPassage extends Card {

    public GuidedPassage() {
        addEffect(EffectSlot.SPELL, new GuidedPassageEffect());
    }
}
