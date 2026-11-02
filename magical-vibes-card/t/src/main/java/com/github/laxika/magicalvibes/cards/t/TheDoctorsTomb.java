package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingWithLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.RedistributePlayerLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "580")
public class TheDoctorsTomb extends Card {

    public TheDoctorsTomb() {
        addStaticEffect(new ExileCreaturesInsteadOfDyingWithLifeLossEffect(2));
        addTrigger(Triggers.chaosEnsues(), new RedistributePlayerLifeTotalsEffect());
    }
}
