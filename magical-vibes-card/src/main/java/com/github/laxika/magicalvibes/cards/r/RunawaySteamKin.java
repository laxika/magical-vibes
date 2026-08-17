package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "115")
public class RunawaySteamKin extends Card {

    public RunawaySteamKin() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                new CardColorPredicate(CardColor.RED),
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new NotCondition(new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.PLUS_ONE_PLUS_ONE),
                        new AwardManaEffect(ManaColor.RED, 3)
                ),
                "Remove three +1/+1 counters from Runaway Steam-Kin: Add {R}{R}{R}."
        ));
    }
}
