package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "158")
public class ColossalGrowth extends Card {

    public ColossalGrowth() {
        // Kicker {R}
        addEffect(EffectSlot.STATIC, new KickerEffect("{R}"));

        // Target creature gets +3/+3 until end of turn.
        // If this spell was kicked, instead that creature gets +4/+4 and gains trample and haste
        // until end of turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ConditionalReplacementEffect(new Kicked(),
                        new BoostTargetCreatureEffect(3, 3),
                        SequenceEffect.of(
                                new BoostTargetCreatureEffect(4, 4),
                                new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.TARGET))));
    }
}
