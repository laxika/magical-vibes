package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "AVR", collectorNumber = "100")
public class EssenceHarvest extends Card {

    public EssenceHarvest() {
        // Target player loses X life and you gain X life, where X is the greatest power among creatures you control.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(new GreatestPowerAmongControlled(), LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new GreatestPowerAmongControlled()));
    }
}
