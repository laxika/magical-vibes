package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "63")
public class MirrorshellCrab extends Card {

    public MirrorshellCrab() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(3));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new CounterUnlessPaysEffect(3)),
                "Channel — {2}{U}, Discard this card: Counter target spell or ability unless its controller pays {3}.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryHasTargetPredicate(),
                        "Target must be a spell or ability on the stack."
                )
        ));
    }
}
