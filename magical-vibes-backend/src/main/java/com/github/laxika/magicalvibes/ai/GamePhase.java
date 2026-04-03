package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * Represents the phase of the game based on mana development, used to adjust
 * spell evaluation. Early game prioritizes board development (cheap creatures,
 * tempo); late game prioritizes card advantage and high-impact spells.
 */
public enum GamePhase {

    /**
     * 0–3 mana sources. Board development is critical: cheap creatures
     * and tempo plays are at their highest value. Card draw is weak
     * because you need board presence, not more cards.
     */
    EARLY,

    /**
     * 4–5 mana sources. Balanced phase where most cards are at their
     * baseline value. The AI has enough mana to cast mid-range threats
     * and removal efficiently.
     */
    MID,

    /**
     * 6+ mana sources. Card advantage and finishers dominate. Small
     * creatures are outclassed, while board wipes and big threats shine.
     */
    LATE;

    /**
     * Determines the current game phase based on the AI's mana source count.
     * Counts lands and non-land permanents with mana-producing tap abilities.
     */
    public static GamePhase determine(GameData gameData, UUID aiPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        int manaSourceCount = 0;
        for (Permanent perm : battlefield) {
            if (perm.getCard().hasType(CardType.LAND)) {
                manaSourceCount++;
            } else if (hasManaAbility(perm)) {
                manaSourceCount++;
            }
        }

        if (manaSourceCount <= 3) return EARLY;
        if (manaSourceCount <= 5) return MID;
        return LATE;
    }

    private static boolean hasManaAbility(Permanent perm) {
        return perm.getCard().getEffects(com.github.laxika.magicalvibes.model.EffectSlot.ON_TAP).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.ManaProducingEffect.class::isInstance);
    }
}
