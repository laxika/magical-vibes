package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "39")
public class TrainOfThought extends Card {

    public TrainOfThought() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{1}{U}")));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{1}{U}"));
    }
}
