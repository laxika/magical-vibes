package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageFromTargetAttackingCreatureToSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "23")
public class ShieldDancer extends Card {

    public ShieldDancer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new RedirectNextCombatDamageFromTargetAttackingCreatureToSelfEffect()),
                "{2}{W}: The next time target attacking creature would deal combat damage to this creature this turn, that creature deals that damage to itself instead.",
                TargetFilters.attackingCreature()
        ));
    }
}
