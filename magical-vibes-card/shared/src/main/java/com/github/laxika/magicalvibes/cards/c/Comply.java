package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOpponentsCantCastUntilNextTurnEffect;

/**
 * Comply — back half of Failure // Comply.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Choose a card name. Until your
 * next turn, your opponents can't cast spells with the chosen name.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Comply extends Card {

    public Comply() {
        // Choose a card name. Until your next turn, your opponents can't cast spells with the chosen name.
        addEffect(EffectSlot.SPELL, new ChooseCardNameOpponentsCantCastUntilNextTurnEffect());

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{W}"));
    }
}
