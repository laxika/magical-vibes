package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "185")
public class LeagueGuildmage extends Card {

    public LeagueGuildmage() {
        // {3}{U}, {T}: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(new DrawCardEffect(1)),
                "{3}{U}, {T}: Draw a card."
        ));

        // {X}{R}, {T}: Copy target instant or sorcery spell you control with mana value X. You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}",
                List.of(new CopySpellEffect()),
                "{X}{R}, {T}: Copy target instant or sorcery spell you control with mana value X. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByPredicate(),
                                new StackEntryManaValueEqualsXPredicate()
                        )),
                        "Target must be an instant or sorcery spell you control with mana value X."
                )
        ));
    }
}
