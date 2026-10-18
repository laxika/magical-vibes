package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Copy count for "copy it for each other instant and sorcery spell you've cast this turn" (Show of
 * Confidence). Used as the {@link StormCopyEffect#resolutionCount()} descriptor, so the count is taken
 * when the copy trigger resolves and includes spells cast in response to it.
 */
public record OtherInstantsAndSorceriesCastCountEffect() implements SpellCastCopyTriggerEffect {

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId) {
        return copyCount(gameData, castingPlayerId, null);
    }

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId, Card spellCard) {
        return (int) gameData.getSpellsCastThisTurn(castingPlayerId).stream()
                .filter(spell -> spellCard == null || !spell.getId().equals(spellCard.getId()))
                .filter(spell -> spell.hasType(CardType.INSTANT) || spell.hasType(CardType.SORCERY))
                .count();
    }

    @Override
    public boolean tokenCopy() {
        return false;
    }
}
