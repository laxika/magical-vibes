package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "96")
public class KazarovSengirPureblood extends Card {

    public KazarovSengirPureblood() {
        // Whenever a creature an opponent controls is dealt damage, put a +1/+1 counter on Kazarov.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE,
                new PutCounterOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // {3}{R}: Kazarov, Sengir Pureblood deals 2 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{R}",
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{3}{R}: Kazarov, Sengir Pureblood deals 2 damage to target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
