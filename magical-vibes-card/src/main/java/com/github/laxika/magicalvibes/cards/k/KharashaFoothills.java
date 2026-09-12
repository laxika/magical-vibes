package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OPC2", collectorNumber = "22")
public class KharashaFoothills extends Card {

    public KharashaFoothills() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new SacrificeAnyNumberOfPermanentsEffect(new PermanentIsCreaturePredicate()),
                ConditionalEffect.unless(
                        new EventValueAtLeast(1),
                        new QueueReflexiveAbilityEffect(
                                new DealDamageToTargetCreatureEffect(new EventValue()), false, true))));
    }
}
