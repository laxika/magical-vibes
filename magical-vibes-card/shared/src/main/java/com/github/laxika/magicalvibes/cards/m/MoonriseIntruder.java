package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

/**
 * Moonrise Intruder — back face of Village Messenger.
 * 2/2 Werewolf with Menace.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Moonrise Intruder.
 */
public class MoonriseIntruder extends Card {

    public MoonriseIntruder() {
        // Menace is loaded from Scryfall.

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Moonrise Intruder.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
