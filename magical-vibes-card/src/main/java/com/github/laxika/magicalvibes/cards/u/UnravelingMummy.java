package com.github.laxika.magicalvibes.cards.u;

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

@CardRegistration(set = "HOU", collectorNumber = "147")
public class UnravelingMummy extends Card {

    public UnravelingMummy() {
        // Only creatures attack, so "attacking Zombie" is attacking + Zombie subtype.
        var attackingZombie = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                "Target must be an attacking Zombie");

        // {1}{W}: Target attacking Zombie gains lifelink until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                "{1}{W}: Target attacking Zombie gains lifelink until end of turn.",
                attackingZombie));

        // {1}{B}: Target attacking Zombie gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{1}{B}: Target attacking Zombie gains deathtouch until end of turn.",
                attackingZombie));
    }
}
