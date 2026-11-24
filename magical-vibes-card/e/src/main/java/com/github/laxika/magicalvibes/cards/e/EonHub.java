package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "5DN", collectorNumber = "120")
public class EonHub extends Card {

    public EonHub() {
        addStaticEffect(new SkipDrawStepEffect(SkipStepOrPhaseKind.UPKEEP_STEP, Subjects.eachPlayer()));
    }
}
