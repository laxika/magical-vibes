package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Trigger descriptor for Coststorm: copies the spell for each different mana value among the
 * controller's other spells and lands played this turn.
 */
public record CoststormEffect() implements SpellCastCopyTriggerEffect {

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId) {
        List<Card> spells = gameData.getSpellsCastThisTurn(castingPlayerId);
        Set<Integer> manaValues = new HashSet<>();
        int spellsToCount = Math.max(0, spells.size() - 1);
        for (int i = 0; i < spellsToCount; i++) {
            manaValues.add(spells.get(i).getManaValue());
        }
        if (gameData.landsPlayedThisTurn.getOrDefault(castingPlayerId, 0) > 0) {
            manaValues.add(0);
        }
        return manaValues.size();
    }

    @Override
    public boolean tokenCopy() {
        return false;
    }
}
