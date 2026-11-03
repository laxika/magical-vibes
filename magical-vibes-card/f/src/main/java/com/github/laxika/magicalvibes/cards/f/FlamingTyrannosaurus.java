package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "85")
@CardRegistration(set = "WHO", collectorNumber = "385")
@CardRegistration(set = "WHO", collectorNumber = "690")
@CardRegistration(set = "WHO", collectorNumber = "976")
public class FlamingTyrannosaurus extends Card {

    public FlamingTyrannosaurus() {
        // Paradox — Whenever you cast a spell from anywhere other than your hand, this creature
        // deals 3 damage to any target. Then put a +1/+1 counter on this creature.
        List<CardEffect> paradox = List.of(
                new DealDamageToAnyTargetEffect(3),
                new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND), paradox.toArray(CardEffect[]::new));

        // When this creature dies, it deals damage equal to its power to each opponent.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
    }
}
