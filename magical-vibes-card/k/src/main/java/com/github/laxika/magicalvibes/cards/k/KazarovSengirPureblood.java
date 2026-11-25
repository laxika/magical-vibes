package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "96")
public class KazarovSengirPureblood extends Card {

    public KazarovSengirPureblood() {
        // Whenever a creature an opponent controls is dealt damage, put a +1/+1 counter on Kazarov.
        addTrigger(Triggers.creatureOpponentControlsDealtDamage(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        // {3}{R}: Kazarov, Sengir Pureblood deals 2 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{R}",
                List.of(DealDamageEffect.toTargetCreature(2)),
                "{3}{R}: Kazarov, Sengir Pureblood deals 2 damage to target creature.",
                TargetFilters.creature()));
    }
}
