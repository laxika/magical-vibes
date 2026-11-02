package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "69")
@CardRegistration(set = "VMA", collectorNumber = "121")
public class FledglingDjinn extends Card {

    public FledglingDjinn() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
