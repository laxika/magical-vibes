package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OPC2", collectorNumber = "22")
@CardRegistration(set = "OPCA", collectorNumber = "43")
@CardRegistration(set = "MOC", collectorNumber = "149")
public class KharashaFoothills extends Card {

    public KharashaFoothills() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingOpponentOfSourceControllerPredicate(),
                new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect()));
        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new SacrificeAnyNumberOfPermanentsEffect(new PermanentIsCreaturePredicate()),
                new ConditionalEffect(
                        new EventValueAtLeast(1),
                        new DealDamageToTargetCreatureEffect(new EventValue()))));
    }
}
