package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "40")
public class Vacuumelt extends Card {

    public Vacuumelt() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}{U}")));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{2}{U}"));
    }
}
