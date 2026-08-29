package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "6")
public class CharmPeddler extends Card {

    public CharmPeddler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        PreventDamageFromChosenSourceEffect.nextDamageToTargetCreature()
                ),
                "{W}, {T}, Discard a card: The next time a source of your choice would deal damage to target creature this turn, prevent that damage.",
                TargetFilters.creature()
        ));
    }
}
