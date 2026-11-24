package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "16")
public class FightOrFlight extends Card {

    public FightOrFlight() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.OPPONENT), SequenceEffect.of(
                SeparateIntoPilesEffect.permanents(
                        Subjects.controlledBy(Subjects.activePlayer(), new PermanentIsCreaturePredicate()), "creatures"),
                new CantAttackThisTurnEffect(Subjects.except(
                        Subjects.controlledBy(Subjects.activePlayer(), new PermanentIsCreaturePredicate()),
                        Subjects.thisWay(StepKind.CHOOSE)))));
    }
}
