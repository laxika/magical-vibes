package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "176")
public class UlrichsKindred extends Card {

    public UlrichsKindred() {
        // Trample is auto-loaded from Scryfall.
        // {3}{G}: Target attacking Wolf or Werewolf gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{G}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)),
                "{3}{G}: Target attacking Wolf or Werewolf gains indestructible until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.WOLF),
                                        new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF))))),
                        "Target must be an attacking Wolf or Werewolf")));
    }
}
