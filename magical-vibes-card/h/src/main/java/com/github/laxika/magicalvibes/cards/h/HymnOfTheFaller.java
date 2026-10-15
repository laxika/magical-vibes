package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "EOE", collectorNumber = "107")
public class HymnOfTheFaller extends Card {

    public HymnOfTheFaller() {
        addEffect(EffectSlot.SPELL, new SurveilEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(1));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new VoidCondition(), new DrawCardEffect()));
    }
}
