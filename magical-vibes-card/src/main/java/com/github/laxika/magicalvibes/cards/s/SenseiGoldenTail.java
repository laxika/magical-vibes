package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "44")
public class SenseiGoldenTail extends Card {

    public SenseiGoldenTail() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.TRAINING),
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_BLOCK,
                                new BushidoEffect(1),
                                EffectDuration.PERMANENT,
                                false),
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_BECOMES_BLOCKED,
                                new BushidoEffect(1),
                                EffectDuration.PERMANENT,
                                false),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.SAMURAI)
                ),
                "{1}{W}, {T}: Put a training counter on target creature. That creature gains bushido 1 and becomes a Samurai in addition to its other creature types. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
