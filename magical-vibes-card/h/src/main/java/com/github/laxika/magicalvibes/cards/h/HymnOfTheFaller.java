package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "EOE", collectorNumber = "107")
public class HymnOfTheFaller extends Card {

    public HymnOfTheFaller() {
        addSpellEffect(new SurveilEffect(1));
        addSpellEffect(new DrawCardEffect());
        addSpellEffect(new LoseLifeEffect(1));
        addSpellEffect(new ConditionalEffect(new VoidCondition(), new DrawCardEffect()));
    }
}
