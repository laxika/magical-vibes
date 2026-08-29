package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "72")
public class Pyromatics extends Card {

    public Pyromatics() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{1}{R}")));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{1}{R}"));
    }
}
