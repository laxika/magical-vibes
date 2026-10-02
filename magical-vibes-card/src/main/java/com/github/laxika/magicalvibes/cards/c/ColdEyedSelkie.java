package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "EVE", collectorNumber = "149")
@CardRegistration(set = "MMA", collectorNumber = "186")
@CardRegistration(set = "DDT", collectorNumber = "25")
@CardRegistration(set = "C15", collectorNumber = "241")
@CardRegistration(set = "AFC", collectorNumber = "183")
@CardRegistration(set = "OTC", collectorNumber = "218")
@CardRegistration(set = "LCC", collectorNumber = "266")
@CardRegistration(set = "C20", collectorNumber = "204")
@CardRegistration(set = "C18", collectorNumber = "172")
public class ColdEyedSelkie extends Card {

    public ColdEyedSelkie() {
        // Islandwalk is auto-loaded from Scryfall.
        // Whenever Cold-Eyed Selkie deals combat damage to a player, you may draw that many cards.
        // EventValue reads the combat damage dealt, wired onto the may-trigger by CombatDamageService.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DrawCardEffect(new EventValue()), "Draw that many cards?"));
    }
}
