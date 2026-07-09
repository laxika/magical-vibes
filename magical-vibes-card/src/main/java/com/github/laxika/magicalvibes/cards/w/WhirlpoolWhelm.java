package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "96")
public class WhirlpoolWhelm extends Card {

    public WhirlpoolWhelm() {
        // Clash with an opponent, then return target creature to its owner's hand. If you win, you
        // may put that creature on top of its owner's library instead.
        //
        // The bare clash runs first and records its result. On a win the WonClash-gated "you may put
        // it on top" resolves before the bounce: accepting tucks the creature (so the trailing bounce
        // finds nothing and no-ops), declining or losing the clash falls through to the bounce — which
        // is exactly the "on top of its owner's library instead" replacement.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new ClashEffect(null))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new WonClash(),
                        new MayEffect(new PutTargetOnTopOfLibraryEffect(),
                                "You may put that creature on top of its owner's library instead.")))
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
