package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MAT", collectorNumber = "30")
public class FeastOfTheVictoriousDead extends Card {

    public FeastOfTheVictoriousDead() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new Morbid(),
                SequenceEffect.of(
                        new GainLifeEffect(new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)),
                        new DistributeCountersAmongControlledCreaturesEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER))
                )));
    }
}
