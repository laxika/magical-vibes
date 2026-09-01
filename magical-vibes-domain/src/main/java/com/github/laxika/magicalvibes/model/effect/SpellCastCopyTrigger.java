package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * A spell-cast copy trigger that can be stored in an emblem and materialized when a spell is cast.
 */
public interface SpellCastCopyTrigger extends CardEffect {

    boolean matches(Card spellCard);

    CardEffect createCopyEffect(StackEntry spellSnapshot, UUID castingPlayerId, int copies);
}
