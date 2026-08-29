package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "5")
public class GuardDogs extends Card {

    public GuardDogs() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffect()),
                "{2}{W}, {T}: Choose a permanent you control. Prevent all combat damage target creature would deal this turn if it shares a color with that permanent.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }
}
