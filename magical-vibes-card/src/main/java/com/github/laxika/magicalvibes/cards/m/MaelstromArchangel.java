package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "115")
public class MaelstromArchangel extends Card {

    public MaelstromArchangel() {
        // Flying is loaded from Scryfall.
        // Whenever this creature deals combat damage to a player, you may cast a spell from your
        // hand without paying its mana cost.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect());
    }
}
