package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "INV", collectorNumber = "102")
public class DoOrDie extends Card {

    public DoOrDie() {
        addSpellEffect(SeparateIntoPilesEffect.permanents(
                Subjects.controlledBy(Subjects.targetPlayer(), new PermanentIsCreaturePredicate()), "creatures"));
        addSpellEffect(new DestroyTargetPermanentEffect(
                Subjects.thisWay(StepKind.CHOOSE, new PermanentIsCreaturePredicate()), true));
    }
}
