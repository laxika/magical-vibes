package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "293")
public class JadeMonolith extends Card {

    public JadeMonolith() {
        // {1}: The next time a source of your choice would deal damage to target creature this turn, that
        // source deals that damage to you instead.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect()),
                "{1}: The next time a source of your choice would deal damage to target creature this turn, that source deals that damage to you instead.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));
    }
}
