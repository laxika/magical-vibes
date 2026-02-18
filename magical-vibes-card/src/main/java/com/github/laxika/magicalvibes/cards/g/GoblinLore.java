package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RandomDiscardEffect;

public class GoblinLore extends Card {

    public GoblinLore() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        addEffect(EffectSlot.SPELL, new RandomDiscardEffect(3));
    }
}
