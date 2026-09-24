package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "49")
public class LoseFocus extends Card {

    public LoseFocus() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{U}")));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{U}"));
    }
}
