package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Trigger descriptor for Coststorm: copies the spell for each different mana value among the
 * controller's other spells and lands played this turn. The count is evaluated when the trigger
 * resolves, so spells cast in response to it are counted, and a spell's mana value on the stack
 * includes its chosen X.
 */
public record CoststormEffect() implements SpellCastCopyTriggerEffect {

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId) {
        return copyCount(gameData, castingPlayerId, null);
    }

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId, Card spellCard) {
        Set<Integer> manaValues = new HashSet<>();
        for (Card spell : gameData.getSpellsCastThisTurn(castingPlayerId)) {
            if (spellCard == null || !spell.getId().equals(spellCard.getId())) {
                manaValues.add(gameData.getSpellCastStackManaValue(spell));
            }
        }
        for (Card land : gameData.landCardsPlayedThisTurn.getOrDefault(castingPlayerId, List.of())) {
            manaValues.add(land.getManaValue());
        }
        return manaValues.size();
    }

    @Override
    public boolean tokenCopy() {
        return false;
    }
}
