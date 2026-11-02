package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "98")
@CardRegistration(set = "PIP", collectorNumber = "215")
@CardRegistration(set = "PIP", collectorNumber = "477")
@CardRegistration(set = "PIP", collectorNumber = "743")
@CardRegistration(set = "PIP", collectorNumber = "1005")
public class FerventCharge extends Card {

    public FerventCharge() {
        addTrigger(Triggers.creatureYouControlAttacks(), new BoostTargetCreatureEffect(2, 2));
    }
}
