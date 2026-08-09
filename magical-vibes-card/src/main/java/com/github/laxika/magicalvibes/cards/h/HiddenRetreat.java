package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STH", collectorNumber = "6")
public class HiddenRetreat extends Card {

    public HiddenRetreat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PutCardFromHandOnTopOfLibraryCost(), new PreventDamageFromTargetSpellEffect()),
                "Put a card from your hand on top of your library: Prevent all damage that would be dealt by target instant or sorcery spell this turn.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        "Target must be an instant or sorcery spell.")));
    }
}
