package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "8")
@CardRegistration(set = "OPCA", collectorNumber = "22")
public class TheEonFog extends Card {

    public TheEonFog() {
        addStaticEffect(new SkipDrawStepEffect(SkipStepOrPhaseKind.UNTAP_STEP, Subjects.eachPlayer()));
        addTrigger(Triggers.chaosEnsues(), new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you())));
    }
}
