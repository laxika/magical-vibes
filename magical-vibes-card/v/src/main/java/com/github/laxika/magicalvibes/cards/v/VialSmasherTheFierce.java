package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1202")
@CardRegistration(set = "FCA", collectorNumber = "59")
@CardRegistration(set = "DSC", collectorNumber = "239")
@CardRegistration(set = "C16", collectorNumber = "49")
public class VialSmasherTheFierce extends Card {

    public VialSmasherTheFierce() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(1),
                SequenceEffect.of(
                        new ChoosePlayerAtRandomEffect(Subjects.eachOpponent()),
                        DealDamageEffect.toPlayerOrTheirPlaneswalker(new TargetSpellManaValue(),
                                Subjects.playersThisWay(StepKind.CHOOSE))));
    }
}
