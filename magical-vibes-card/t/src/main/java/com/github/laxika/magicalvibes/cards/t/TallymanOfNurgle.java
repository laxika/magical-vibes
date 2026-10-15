package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "40K", collectorNumber = "60")
public class TallymanOfNurgle extends Card {

    public TallymanOfNurgle() {
        // At the beginning of your end step, if a creature died this turn, draw a card and lose
        // 1 life. If seven or more creatures died this turn, draw six additional cards and lose
        // six additional life, for the printed total of seven cards and seven life.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new CreaturesDiedThisTurnAtLeast(1),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1),
                        new ConditionalEffect(
                                new CreaturesDiedThisTurnAtLeast(7),
                                SequenceEffect.of(new DrawCardEffect(6), new LoseLifeEffect(6))))));
    }
}
