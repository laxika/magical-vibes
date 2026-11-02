package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "89")
@CardRegistration(set = "C14", collectorNumber = "137")
@CardRegistration(set = "SCD", collectorNumber = "69")
public class BloodgiftDemon extends Card {

    public BloodgiftDemon() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DrawCardForTargetPlayerEffect(1, false, true));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
    }
}
