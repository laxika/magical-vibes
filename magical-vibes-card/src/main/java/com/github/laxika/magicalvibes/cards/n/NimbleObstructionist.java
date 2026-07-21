package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "40")
public class NimbleObstructionist extends Card {

    public NimbleObstructionist() {
        // Flash and Flying come from Scryfall.
        // Cycling {2}{U} ({2}{U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, counter target activated or triggered ability you don't control."
        // The reflexive cycle trigger rides on the cycling ability: the counter resolves first, then
        // the cycling draw. minTargets 0 lets cycling proceed (and still draw) when there is no legal
        // ability to counter (CR 603.3c) — you can't counter a mana ability, which never uses the stack.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{U}",
                List.of(new CounterSpellEffect(), new DrawCardEffect(1)),
                "Cycling {2}{U} ({2}{U}, Discard this card: Draw a card.)",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryNotPredicate(new StackEntryControlledByPredicate()))),
                        "Target must be an activated or triggered ability you don't control."),
                null, null, null, List.of(), 0, 1));
    }
}
