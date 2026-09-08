package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotTargetedByNamedCreatureAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHR", collectorNumber = "48")
public class GoblinArtisans extends Card {

    public GoblinArtisans() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new FlipCoinWinEffect(
                        new DrawCardEffect(1),
                        new CounterSpellEffect())),
                "{T}: Flip a coin. If you win the flip, draw a card. If you lose the flip, counter target "
                        + "artifact spell you control that isn't the target of an ability from another creature "
                        + "named Goblin Artisans.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ARTIFACT_SPELL)),
                                new StackEntryControlledByPredicate(),
                                new StackEntryNotTargetedByNamedCreatureAbilityPredicate("Goblin Artisans"))),
                        "Target must be an artifact spell you control that isn't already targeted by another Goblin Artisans ability."))
        );
    }
}
