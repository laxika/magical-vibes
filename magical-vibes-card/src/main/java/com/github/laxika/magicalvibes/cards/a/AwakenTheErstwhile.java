package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenCreatesTokensEffect;

@CardRegistration(set = "RNA", collectorNumber = "61")
public class AwakenTheErstwhile extends Card {

    public AwakenTheErstwhile() {
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsHandThenCreatesTokensEffect());
    }
}
