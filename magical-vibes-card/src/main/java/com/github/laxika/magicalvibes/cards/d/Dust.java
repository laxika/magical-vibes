package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Dust — back half of Grind // Dust.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Exile any number of target
 * creatures that have -1/-1 counters on them.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Dust extends Card {

    public Dust() {
        // Exile any number of target creatures that have -1/-1 counters on them.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE)
                )),
                "Target must be a creature that has a -1/-1 counter on it"
        ), 0, 99)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{3}{W}"));
    }
}
