package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

@CardRegistration(set = "YSOS", collectorNumber = "4")
public class InterdisciplinaryStudies extends Card {

    public InterdisciplinaryStudies() {
        addSpellEffect(new SeekEffect(new CardIsMulticoloredPredicate()));
        addSpellEffect(new LearnEffect());
    }
}
