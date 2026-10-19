package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "40K", collectorNumber = "60")
public class TallymanOfNurgle extends Card {

    public TallymanOfNurgle() {
        // At the beginning of your end step, if a creature died this turn, you draw a card and you
        // lose 1 life. If seven or more creatures died this turn, instead you draw seven cards and you
        // lose 7 life.
        CreaturesDiedThisTurnAtLeast sevenDeaths = new CreaturesDiedThisTurnAtLeast(7);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new CreaturesDiedThisTurnAtLeast(1),
                SequenceEffect.of(
                        new ConditionalEffect(sevenDeaths,
                                SequenceEffect.of(new DrawCardEffect(7), new LoseLifeEffect(7))),
                        new ConditionalEffect(new NotCondition(sevenDeaths),
                                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))))));
    }
}
