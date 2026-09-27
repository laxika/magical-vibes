package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Trigger descriptor for a spell that copies itself once for each time its controller has cast a
 * commander from the command zone this game.
 */
public record CopyThisSpellForEachCommanderCastEffect() implements SpellCastCopyTriggerEffect {

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId) {
        return gameData.playerCommanders.getOrDefault(castingPlayerId, java.util.List.of()).stream()
                .mapToInt(commander -> gameData.commanderTaxByCardId.getOrDefault(commander.getId(), 0) / 2)
                .sum();
    }

    @Override
    public boolean tokenCopy() {
        return true;
    }
}
