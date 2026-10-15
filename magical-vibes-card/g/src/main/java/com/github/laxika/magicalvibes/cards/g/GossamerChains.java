package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "6")
public class GossamerChains extends Card {

    public GossamerChains() {
        // Return this enchantment to its owner's hand: Prevent all combat damage that would be
        // dealt by target unblocked creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ReturnSelfToHandCost(), PreventDamageEffect.allCombatByTargetCreatures()),
                "Return this enchantment to its owner's hand: Prevent all combat damage that would be "
                        + "dealt by target unblocked creature this turn.",
                TargetFilters.unblockedAttackingCreature()
        ));
    }
}
