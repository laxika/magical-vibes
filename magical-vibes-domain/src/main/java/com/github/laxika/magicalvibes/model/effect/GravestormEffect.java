package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Trigger descriptor for the gravestorm keyword. Its copy count is the number of permanents put
 * into graveyards from the battlefield during the current turn.
 */
public record GravestormEffect() implements SpellCastCopyTriggerEffect {

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId) {
        return gameData.permanentsPutIntoGraveyardFromBattlefieldThisTurn;
    }

    @Override
    public boolean tokenCopy() {
        return false;
    }
}
