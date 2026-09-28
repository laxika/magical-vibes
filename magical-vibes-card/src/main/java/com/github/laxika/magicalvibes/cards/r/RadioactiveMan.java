package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "MSC", collectorNumber = "665")
public class RadioactiveMan extends Card {

    public RadioactiveMan() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()), LoseLifeRecipient.TARGET_PLAYER));
    }
}
