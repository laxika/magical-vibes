package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "WOE", collectorNumber = "189")
public class TerritorialWitchstalker extends Card {

    public TerritorialWitchstalker() {
        // At the beginning of combat on your turn, if you control a creature with power 4 or
        // greater, this creature gets +1/+0 until end of turn and can attack this turn as though
        // it didn't have defender.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4)),
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new CanAttackAsThoughNoDefenderEffect())));
    }
}
