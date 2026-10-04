package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Describes a copy trigger checked when its spell is cast. Most counts are captured at cast
 * time; gravestorm evaluates its count when the trigger resolves.
 */
public interface SpellCastCopyTriggerEffect extends CardEffect {

    int copyCount(GameData gameData, UUID castingPlayerId);

    boolean tokenCopy();
}
