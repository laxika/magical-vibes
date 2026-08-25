package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "RAV", collectorNumber = "67")
public class SurveillingSprite extends Card {

    public SurveillingSprite() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
