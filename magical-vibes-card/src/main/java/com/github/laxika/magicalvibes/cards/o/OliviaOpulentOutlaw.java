package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "2")
public class OliviaOpulentOutlaw extends Card {

    public OliviaOpulentOutlaw() {
        PermanentAnyOfPredicate outlaw = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN),
                new PermanentHasSubtypePredicate(CardSubtype.MERCENARY),
                new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                new PermanentHasSubtypePredicate(CardSubtype.ROGUE),
                new PermanentHasSubtypePredicate(CardSubtype.WARLOCK)
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(outlaw, CreateTokenEffect.ofTreasureToken(1), false, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2,
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate())
                ),
                "{3}, Sacrifice two Treasures: Put two +1/+1 counters on each creature you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
