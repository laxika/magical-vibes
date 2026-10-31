package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CreaturesControllerControlledThatDealtCombatDamageToPlayerThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

/**
 * Excess — back half of Indulge // Excess.
 * Sorcery — Aftermath: create a Treasure token for each creature you controlled that dealt combat
 * damage to a player this turn.
 */
public class Excess extends Card {

    public Excess() {
        addSpellEffect(CreateTokenEffect.ofTreasureToken(
                new CreaturesControllerControlledThatDealtCombatDamageToPlayerThisTurn()));

        addCastingOption(new FlashbackCast("{1}{R}"));
    }
}
