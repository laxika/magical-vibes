package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerHasBoon;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConsumeBoonEffect;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "8")
public class UnderbridgeWarlock extends Card {

    public UnderbridgeWarlock() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                CreateBoonEffect.usedAsItResolves(new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new CreaturesDiedThisTurnAtLeast(3)),
                        SequenceEffect.of(
                                new LoseLifeEffect(5, Subjects.eachOpponent()),
                                new GainLifeEffect(5),
                                new ConsumeBoonEffect()))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerHasBoon()),
                SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(2)));
    }
}
