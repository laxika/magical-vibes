package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "57")
public class CurseOfThirst extends Card {

    public CurseOfThirst() {
        // At the beginning of enchanted player's upkeep, this Aura deals damage to that player
        // equal to the number of Curses attached to them.
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                DealDamageToEnchantedPlayerEffect.attachedCount(new PermanentHasSubtypePredicate(CardSubtype.CURSE)));
    }
}
