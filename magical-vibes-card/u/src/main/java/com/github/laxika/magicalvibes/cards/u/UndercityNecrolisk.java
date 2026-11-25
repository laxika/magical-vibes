package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "87")
public class UndercityNecrolisk extends Card {

    public UndercityNecrolisk() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.MENACE, Subjects.source())
                ),
                "{1}, Sacrifice another creature: Put a +1/+1 counter on Undercity Necrolisk. It gains menace until end of turn.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
