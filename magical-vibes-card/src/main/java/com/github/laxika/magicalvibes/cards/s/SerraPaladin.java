package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "61")
public class SerraPaladin extends Card {

    public SerraPaladin() {
        // {T}: Prevent the next 1 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PreventDamageToTargetEffect(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));

        // {1}{W}{W}, {T}: Target creature gains vigilance until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)),
                "{1}{W}{W}, {T}: Target creature gains vigilance until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }
}
