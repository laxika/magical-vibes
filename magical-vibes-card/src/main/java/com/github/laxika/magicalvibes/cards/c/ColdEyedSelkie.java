package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "EVE", collectorNumber = "149")
public class ColdEyedSelkie extends Card {

    public ColdEyedSelkie() {
        // Islandwalk is auto-loaded from Scryfall.
        // Whenever Cold-Eyed Selkie deals combat damage to a player, you may draw that many cards.
        // EventValue reads the combat damage dealt, wired onto the may-trigger by CombatDamageService.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DrawCardEffect(new EventValue()), "Draw that many cards?"));
    }
}
