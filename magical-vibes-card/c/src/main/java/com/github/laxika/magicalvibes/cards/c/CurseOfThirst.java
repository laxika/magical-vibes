package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "57")
public class CurseOfThirst extends Card {

    public CurseOfThirst() {
        // At the beginning of enchanted player's upkeep, this Aura deals damage to that player
        // equal to the number of Curses attached to them.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                DealDamageToPlayersEffect.enchantedAttachedCount(new PermanentHasSubtypePredicate(CardSubtype.CURSE)));
    }
}
