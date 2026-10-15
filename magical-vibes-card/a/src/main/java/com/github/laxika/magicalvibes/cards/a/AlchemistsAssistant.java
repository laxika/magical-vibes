package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ActivationTimingRestriction.SORCERY_SPEED;
import static com.github.laxika.magicalvibes.model.filter.TargetFilters.creature;

@CardRegistration(set = "TDM", collectorNumber = "71")
public class AlchemistsAssistant extends Card {

    public AlchemistsAssistant() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.LIFELINK, 1)),
                "Renew {1}{B} ({1}{B}, Exile this card from your graveyard: Put a lifelink counter on target creature. Renew only as a sorcery.)",
                creature(),
                null,
                null,
                SORCERY_SPEED));
    }
}
