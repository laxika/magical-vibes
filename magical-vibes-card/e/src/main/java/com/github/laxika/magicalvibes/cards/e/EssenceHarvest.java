package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DeterminedOnce;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "AVR", collectorNumber = "100")
public class EssenceHarvest extends Card {

    public EssenceHarvest() {
        // Target player loses X life and you gain X life, where X is the greatest power among creatures you control.
        // X is determined once, so losing life first cannot change the amount gained (CR 608.2h).
        DeterminedOnce greatestPower = new DeterminedOnce(new GreatestPowerAmongControlled());
        addSpellEffect(new LoseLifeEffect(greatestPower, LoseLifeRecipient.TARGET_PLAYER));
        addSpellEffect(new GainLifeEffect(greatestPower));
    }
}
