package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "86")
public class QarsiRevenant extends Card {

    public QarsiRevenant() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.FLYING),
                        new PutCounterOnTargetPermanentEffect(CounterType.DEATHTOUCH),
                        new PutCounterOnTargetPermanentEffect(CounterType.LIFELINK)),
                "Renew {2}{B} ({2}{B}, Exile this card from your graveyard: Put a flying counter, "
                        + "a deathtouch counter, and a lifelink counter on target creature. Activate only "
                        + "as a sorcery.)",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
