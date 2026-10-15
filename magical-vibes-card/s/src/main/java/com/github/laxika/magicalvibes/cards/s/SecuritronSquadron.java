package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "23")
@CardRegistration(set = "PIP", collectorNumber = "328")
@CardRegistration(set = "PIP", collectorNumber = "551")
@CardRegistration(set = "PIP", collectorNumber = "856")
public class SecuritronSquadron extends Card {

    public SecuritronSquadron() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{3}")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{3}")));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardIsTokenPredicate(),
                        new PutCountersOnEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false)));
    }
}
