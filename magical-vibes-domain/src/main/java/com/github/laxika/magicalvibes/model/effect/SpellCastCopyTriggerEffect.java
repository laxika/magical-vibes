package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Describes a copy trigger checked when its spell is cast. Most counts are captured at cast
 * time; gravestorm, Coststorm and Echo Storm evaluate their counts when the trigger resolves.
 */
public interface SpellCastCopyTriggerEffect extends CardEffect {

    int copyCount(GameData gameData, UUID castingPlayerId);

    /**
     * Count evaluated when the copy trigger resolves, given the spell being copied. Defaults to
     * {@link #copyCount(GameData, UUID)}; overridden by counts that exclude the copied spell itself.
     */
    default int copyCount(GameData gameData, UUID castingPlayerId, Card spellCard) {
        return copyCount(gameData, castingPlayerId);
    }

    boolean tokenCopy();
}
