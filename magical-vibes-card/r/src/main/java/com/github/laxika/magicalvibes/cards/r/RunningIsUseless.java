package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DSC", collectorNumber = "356")
public class RunningIsUseless extends Card {

    public RunningIsUseless() {
        addSpellEffect(ChoosePermanentsEffect.anyNumber(Subjects.allCreatures(), "any number of creatures")
                .withDifferent(ChoosePermanentsEffect.Different.MANA_VALUE));
        addSpellEffect(new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
