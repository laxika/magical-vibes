package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "75")
public class ShatteringSpree extends Card {

    public ShatteringSpree() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{R}")));
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{R}"));
    }
}
