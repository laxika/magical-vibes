package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SUM", collectorNumber = "79")
@CardRegistration(set = "ME1", collectorNumber = "50")
@CardRegistration(set = "3ED", collectorNumber = "79")
@CardRegistration(set = "V09", collectorNumber = "11")
@CardRegistration(set = "EMA", collectorNumber = "70")
@CardRegistration(set = "VMA", collectorNumber = "93")
@CardRegistration(set = "ARN", collectorNumber = "20")
public class SerendibEfreet extends Card {

    public SerendibEfreet() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
