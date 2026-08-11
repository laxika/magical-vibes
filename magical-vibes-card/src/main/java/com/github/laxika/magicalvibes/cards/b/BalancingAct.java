package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsDownToFewestEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToFewestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ODY", collectorNumber = "10")
public class BalancingAct extends Card {

    public BalancingAct() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerSacrificesDownToFewestEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsDownToFewestEffect());
    }
}
