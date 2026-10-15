package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "108")
public class HomicidalSeclusion extends Card {

    public HomicidalSeclusion() {
        // As long as you control exactly one creature, that creature gets +3/+1 and has lifelink.
        // "Exactly one" is the conjunction of the at-least-one and at-most-one creature counts;
        // while it holds, the only creature you control is the sole recipient of an
        // own-creatures boost.
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(1, new PermanentIsCreaturePredicate()),
                                new ControlsPermanentCountAtMost(1, new PermanentIsCreaturePredicate()))),
                        new StaticBoostEffect(3, 1, Set.of(Keyword.LIFELINK), GrantScope.OWN_CREATURES)));
    }
}
