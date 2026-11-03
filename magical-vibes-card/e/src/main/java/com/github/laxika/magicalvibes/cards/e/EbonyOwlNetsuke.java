package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "154")
public class EbonyOwlNetsuke extends Card {

    public EbonyOwlNetsuke() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerHandAtLeast(7)),
                new DealDamageToPlayersEffect(4, DamageRecipient.ACTIVE_PLAYER));
    }
}
