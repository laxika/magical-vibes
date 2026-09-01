package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "37")
public class ShatterTheSky extends Card {

    public ShatterTheSky() {
        PermanentPredicate creaturePowerAtLeastFour = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)));
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(
                new FixedIfCondition(new ControlsPermanentCount(1, creaturePowerAtLeastFour), 1, 0)));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
