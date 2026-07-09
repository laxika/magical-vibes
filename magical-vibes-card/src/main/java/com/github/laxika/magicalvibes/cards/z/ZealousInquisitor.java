package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "57")
public class ZealousInquisitor extends Card {

    public ZealousInquisitor() {
        // {1}{W}: The next 1 damage that would be dealt to this creature this turn is dealt to target creature instead.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new RedirectNextDamageToTargetCreatureEffect(1)),
                "{1}{W}: The next 1 damage that would be dealt to this creature this turn is dealt to target creature instead.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));
    }
}
