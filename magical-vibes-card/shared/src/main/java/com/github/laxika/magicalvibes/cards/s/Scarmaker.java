package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.h.HiredMuscle}.
 */
public class Scarmaker extends Card {

    public Scarmaker() {
        // "Remove a ki counter from Scarmaker: Target creature gains fear until end of turn."
        // - the ki counters carry over from the unflipped face, so this has no mana cost and no tap.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.KI),
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET)
                ),
                "Remove a ki counter from Scarmaker: Target creature gains fear until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
