package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

public class ProfaneInsight extends Card {

    public ProfaneInsight() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(1));
    }
}
