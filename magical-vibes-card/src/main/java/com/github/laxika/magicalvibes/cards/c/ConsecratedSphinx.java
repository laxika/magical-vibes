package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MBS", collectorNumber = "21")
public class ConsecratedSphinx extends Card {

    public ConsecratedSphinx() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new MayEffect(new DrawCardEffect(2), "Draw two cards?"));
    }
}
