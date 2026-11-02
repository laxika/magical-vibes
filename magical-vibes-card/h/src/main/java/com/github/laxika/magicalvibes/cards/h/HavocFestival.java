package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "166")
public class HavocFestival extends Card {

    public HavocFestival() {
        addStaticEffect(new PlayersCantGainLifeEffect());

        // Triggers.upkeep(ActivePlayer.ANY) sets the active player as targetId — "that player loses half their life, rounded up".
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()), LoseLifeRecipient.ACTIVE_PLAYER));
    }
}
