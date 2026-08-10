package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;

@CardRegistration(set = "RTR", collectorNumber = "166")
public class HavocFestival extends Card {

    public HavocFestival() {
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());

        // EACH_UPKEEP_TRIGGERED sets the active player as targetId — "that player loses half their life, rounded up".
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()), LoseLifeRecipient.ACTIVE_PLAYER));
    }
}
