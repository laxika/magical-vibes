package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "171")
public class StandOrFall extends Card {

    public StandOrFall() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                SeparateIntoPilesEffect.permanents(
                        Subjects.controlledBy(Subjects.eachOpponent(), new PermanentIsCreaturePredicate()), "creatures"),
                new CantBlockThisTurnEffect(Subjects.except(
                        Subjects.controlledBy(Subjects.eachOpponent(), new PermanentIsCreaturePredicate()),
                        Subjects.thisWay(StepKind.CHOOSE)))));
    }
}
