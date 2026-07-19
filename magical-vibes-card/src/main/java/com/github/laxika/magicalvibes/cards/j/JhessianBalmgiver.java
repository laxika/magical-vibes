package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "112")
public class JhessianBalmgiver extends Card {

    public JhessianBalmgiver() {
        // {T}: Prevent the next 1 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(PreventDamageEffect.nextToAny(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."));

        // {T}: Target creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new MakeCreatureUnblockableEffect()),
                "{T}: Target creature can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )));
    }
}
