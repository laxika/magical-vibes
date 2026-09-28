package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Counters the target spell and independently offers one matching card from the controller's
 * graveyard for a free cast, limited by the target spell's effective mana value.
 */
public record CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect(
        CardPredicate cardFilter
) implements CounterSpellingEffect {

    public CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
