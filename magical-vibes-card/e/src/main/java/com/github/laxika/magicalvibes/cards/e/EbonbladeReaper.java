package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ONS", collectorNumber = "141")
public class EbonbladeReaper extends Card {

    public EbonbladeReaper() {
        addMorph("{3}{B}{B}");
        addEffect(EffectSlot.ON_ATTACK,
                new LoseLifeEffect(new HalfControllerLifeRoundedUp(), LoseLifeRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
