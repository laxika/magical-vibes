package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "32")
public class SanctuaryWall extends Card {

    public SanctuaryWall() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(
                        new TapPermanentsEffect(Subjects.target()),
                        new MayEffect(
                                SequenceEffect.of(
                                        new PutCountersEffect(CounterType.STUN),
                                        PutCountersEffect.onSelf(CounterType.STUN)
                                ),
                                "Put a stun counter on it?"
                        )
                ),
                "{2}{W}, {T}: Tap target creature. You may put a stun counter on it. If you do, put a stun counter on this creature.",
                TargetFilters.creature()
        ));
    }
}
