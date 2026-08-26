package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "60")
public class MuddleTheMixture extends Card {

    public MuddleTheMixture() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(new SearchLibraryEffect(null, LibrarySearchDestination.HAND,
                        new ManaValueBound(new LastDiscardedCardManaValue(), true, 0))),
                "Transmute {1}{U}{U} ({1}{U}{U}, Discard this card: Search your library for a card with the same mana value as this card, reveal it, put it into your hand, then shuffle. Transmute only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
