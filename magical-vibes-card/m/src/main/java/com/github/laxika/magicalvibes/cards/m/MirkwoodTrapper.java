package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksNotController;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "62")
@CardRegistration(set = "LTC", collectorNumber = "144")
@CardRegistration(set = "LTC", collectorNumber = "469")
public class MirkwoodTrapper extends Card {

    public MirkwoodTrapper() {
        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.creaturesAttackYou(),
                        new BoostTargetCreatureEffect(-2, 0));

        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new PlayerAttacksNotController()),
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1,
                                Subjects.controlledBy(Subjects.triggeringPlayer(), new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(), new PermanentIsAttackingPredicate()))),
                                "an attacking creature").chosenBy(Subjects.triggeringPlayer()),
                        new BoostTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), 2, 0)));
    }
}
