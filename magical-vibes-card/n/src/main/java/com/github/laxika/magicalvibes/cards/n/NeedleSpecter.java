package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "39")
public class NeedleSpecter extends Card {

    public NeedleSpecter() {
        // Flying and Wither are auto-loaded from Scryfall.
        // Whenever Needle Specter deals combat damage to a player, that player discards that many
        // cards. EventValue reads the combat damage dealt, wired onto the trigger by CombatDamageService.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(new EventValue(), DiscardRecipient.TARGET_PLAYER));
    }
}
