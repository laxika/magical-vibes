package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "56")
public class AccursedHorde extends Card {

    public AccursedHorde() {
        // {1}{B}: Target attacking Zombie gains indestructible until end of turn.
        // Only creatures attack, so "attacking Zombie" is attacking + Zombie subtype.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)),
                "{1}{B}: Target attacking Zombie gains indestructible until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                        "Target must be an attacking Zombie")));
    }
}
