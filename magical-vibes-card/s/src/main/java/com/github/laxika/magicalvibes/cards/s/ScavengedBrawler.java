package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "17")
@CardRegistration(set = "BRC", collectorNumber = "64")
public class ScavengedBrawler extends Card {

    public ScavengedBrawler() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 4),
                        new PutCounterOnTargetPermanentEffect(CounterType.FLYING),
                        new PutCounterOnTargetPermanentEffect(CounterType.VIGILANCE),
                        new PutCounterOnTargetPermanentEffect(CounterType.TRAMPLE),
                        new PutCounterOnTargetPermanentEffect(CounterType.LIFELINK)),
                "{5}, Exile this card from your graveyard: Put four +1/+1 counters, a flying counter, "
                        + "a vigilance counter, a trample counter, and a lifelink counter on target creature. "
                        + "Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
