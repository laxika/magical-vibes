package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

@CardRegistration(set = "YSOS", collectorNumber = "4")
public class InterdisciplinaryStudies extends Card {

    public InterdisciplinaryStudies() {
        addEffect(EffectSlot.SPELL, new SeekEffect(new CardIsMulticoloredPredicate()));
        addEffect(EffectSlot.SPELL, new LearnEffect());
    }
}
