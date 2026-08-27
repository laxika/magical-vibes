package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "145")
public class IzzetGuildmage extends Card {

    public IzzetGuildmage() {
        // {2}{U}: Copy target instant spell you control with mana value 2 or less. You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new CopySpellEffect()),
                "{2}{U}: Copy target instant spell you control with mana value 2 or less. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                                new StackEntryControlledByPredicate(),
                                new StackEntryMaxManaValuePredicate(2)
                        )),
                        "Target must be an instant spell you control with mana value 2 or less."
                )
        ));

        // {2}{R}: Copy target sorcery spell you control with mana value 2 or less. You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new CopySpellEffect()),
                "{2}{R}: Copy target sorcery spell you control with mana value 2 or less. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByPredicate(),
                                new StackEntryMaxManaValuePredicate(2)
                        )),
                        "Target must be a sorcery spell you control with mana value 2 or less."
                )
        ));
    }
}
