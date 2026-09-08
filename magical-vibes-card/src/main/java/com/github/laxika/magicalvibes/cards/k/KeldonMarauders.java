package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PLC", collectorNumber = "102")
public class KeldonMarauders extends Card {

    public KeldonMarauders() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new EnterWithCountersEffect(CounterType.TIME, new Fixed(2)))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1))
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
    }
}
