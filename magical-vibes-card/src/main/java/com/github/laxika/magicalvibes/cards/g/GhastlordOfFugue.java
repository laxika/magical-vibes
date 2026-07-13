package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "162")
public class GhastlordOfFugue extends Card {

    public GhastlordOfFugue() {
        // This creature can't be blocked.
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        // Whenever this creature deals combat damage to a player, that player reveals their hand.
        // You choose a card from it. That player exiles that card. (The damaged player is supplied
        // as the effect's target by CombatDamageService.)
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ChooseCardsFromTargetHandEffect(1, List.of(), List.of(), HandChoiceDestination.EXILE, false));
    }
}
