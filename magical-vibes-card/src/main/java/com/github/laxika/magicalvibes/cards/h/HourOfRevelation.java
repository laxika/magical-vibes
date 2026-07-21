package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "HOU", collectorNumber = "15")
public class HourOfRevelation extends Card {

    public HourOfRevelation() {
        // This spell costs {3} less to cast if there are ten or more nonland permanents on the battlefield.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyPlayerControlsPermanentCount(10, new PermanentNotPredicate(new PermanentIsLandPredicate())),
                new ReduceOwnCastCostEffect(new Fixed(3))));
        // Destroy all nonland permanents.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
