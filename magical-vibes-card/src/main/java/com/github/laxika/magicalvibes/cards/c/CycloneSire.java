package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "54")
public class CycloneSire extends Card {

    public CycloneSire() {
        target(TargetFilters.landYouControl()).addEffect(EffectSlot.ON_DEATH,
                new MayEffect(
                        SequenceEffect.of(
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                                new AnimatePermanentsEffect(
                                        0, 0,
                                        List.of(CardSubtype.ELEMENTAL),
                                        Set.of(Keyword.HASTE),
                                        null, Set.of(),
                                        GrantScope.TARGET, EffectDuration.PERMANENT
                                )
                        ),
                        "Put three +1/+1 counters on target land you control?"
                ));
    }
}
