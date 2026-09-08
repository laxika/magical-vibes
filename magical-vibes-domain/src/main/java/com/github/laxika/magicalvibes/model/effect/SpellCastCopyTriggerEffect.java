package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Describes a copy trigger that is checked when its spell is cast and whose copy count is fixed
 * at that time.
 */
public interface SpellCastCopyTriggerEffect extends CardEffect {

    int copyCount(GameData gameData, UUID castingPlayerId);

    boolean tokenCopy();
}
