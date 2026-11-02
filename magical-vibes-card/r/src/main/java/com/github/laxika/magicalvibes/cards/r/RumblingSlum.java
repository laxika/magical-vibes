package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "126")
@CardRegistration(set = "HOP", collectorNumber = "93")
@CardRegistration(set = "GK2", collectorNumber = "96")
public class RumblingSlum extends Card {

    public RumblingSlum() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DealDamageToPlayersEffect(1, DamageRecipient.EACH_PLAYER));
    }
}
