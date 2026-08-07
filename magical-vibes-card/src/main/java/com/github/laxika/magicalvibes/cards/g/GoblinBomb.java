package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "103")
public class GoblinBomb extends Card {

    public GoblinBomb() {
        // At the beginning of your upkeep, you may flip a coin. If you win the flip, put a fuse
        // counter on this enchantment. If you lose the flip, remove a fuse counter from this enchantment.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new FlipCoinWinEffect(
                                new PutCountersOnSelfEffect(CounterType.FUSE),
                                new RemoveCounterFromSourceEffect(CounterType.FUSE, 1)
                        ),
                        "Flip a coin for Goblin Bomb?"
                ));

        // Remove five fuse counters from this enchantment and sacrifice it:
        // It deals 20 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(5, CounterType.FUSE),
                        new SacrificeSelfCost(),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(20)
                ),
                "Remove five fuse counters from Goblin Bomb and sacrifice it: Goblin Bomb deals 20 damage to target player or planeswalker."
        ));
    }
}
