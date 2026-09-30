package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BoonTrigger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasBoon;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConsumeBoonEffect;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YWOE", collectorNumber = "8")
public class UnderbridgeWarlock extends Card {

    public UnderbridgeWarlock() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CreateBoonEffect.atControllerEndStep(1,
                        new ConditionalEffect(new CreaturesDiedThisTurnAtLeast(3),
                                SequenceEffect.of(
                                        new LoseLifeEffect(5, LoseLifeRecipient.EACH_OPPONENT),
                                        new GainLifeEffect(5),
                                        new ConsumeBoonEffect(BoonTrigger.CONTROLLER_END_STEP)))));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new ControllerHasBoon(),
                        SequenceEffect.of(
                                new MillEffect(3, MillRecipient.CONTROLLER),
                                new DrawCardEffect(1),
                                new LoseLifeEffect(2))));
    }
}
