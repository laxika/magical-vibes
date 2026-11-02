package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsOwnCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "172")
@CardRegistration(set = "C18", collectorNumber = "16")
@CardRegistration(set = "SCD", collectorNumber = "90")
public class LoyalSubordinate extends Card {

    public LoyalSubordinate() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ConditionalEffect(new ControllerControlsOwnCommander(),
                        new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT)));
    }
}
