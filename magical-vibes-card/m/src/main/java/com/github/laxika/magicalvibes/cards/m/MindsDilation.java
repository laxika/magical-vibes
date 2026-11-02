package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "70")
@CardRegistration(set = "SLD", collectorNumber = "1180")
@CardRegistration(set = "SIR", collectorNumber = "82")
@CardRegistration(set = "MSC", collectorNumber = "277")
@CardRegistration(set = "MSC", collectorNumber = "344")
@CardRegistration(set = "OTC", collectorNumber = "101")
public class MindsDilation extends Card {

    public MindsDilation() {
        addTrigger(Triggers.opponentCastsSpell(),
                new ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect());
    }
}
