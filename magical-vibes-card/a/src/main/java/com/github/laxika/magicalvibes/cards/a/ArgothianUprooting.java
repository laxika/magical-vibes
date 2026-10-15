package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBRO", collectorNumber = "12")
public class ArgothianUprooting extends Card {

    public ArgothianUprooting() {
        // Put two +1/+1 counters on each of X target lands you control. They become 0/0 Elemental
        // creatures with reach, haste, and a leaves-the-battlefield Forest trigger. They remain lands.
        targetExactlyX(TargetFilters.landYouControl(), 100)
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                        0, 0,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.REACH, Keyword.HASTE),
                        null, Set.of(),
                        GrantScope.TARGET, EffectDuration.PERMANENT))
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetEffect(
                        EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new ConjureCardNamedOntoBattlefieldEffect(
                                "BRO", "276", Set.of(CardType.LAND)),
                        EffectDuration.PERMANENT,
                        false));
    }
}
