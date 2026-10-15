package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "85")
public class SpawnOfMayhem extends Card {

    public SpawnOfMayhem() {
        // Spectacle {1}{B}{B}
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{B}{B}")),
                new OpponentLostLifeThisTurn(1),
                false));

        // At the beginning of your upkeep, this creature deals 1 damage to each player. Then if
        // you have 10 or less life, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_PLAYER),
                ConditionalEffect.unless(
                        new ControllerLifeAtMost(10),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
