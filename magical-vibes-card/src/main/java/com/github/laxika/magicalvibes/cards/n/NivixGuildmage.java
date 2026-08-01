package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "182")
public class NivixGuildmage extends Card {

    public NivixGuildmage() {
        // {1}{U}{R}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{R}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}{U}{R}: Draw a card, then discard a card."));

        // {2}{U}{R}: Copy target instant or sorcery spell you control. You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{R}",
                List.of(new CopySpellEffect()),
                "{2}{U}{R}: Copy target instant or sorcery spell you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByPredicate()
                        )),
                        "Target must be an instant or sorcery spell you control.")));
    }
}
