package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Package-private utility methods shared across combat sub-services.
 */
final class CombatHelper {

    private CombatHelper() {}

    static boolean isCantAttackOrBlockUnlessEquipped(GameQueryService gameQueryService,
                                                     GameData gameData,
                                                     Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockUnlessEquippedEffect.class::isInstance)
                && !gameQueryService.isEquipped(gameData, creature);
    }

    static boolean isCantBeBlockedDueToDefenderCondition(GameQueryService gameQueryService,
                                                          GameData gameData,
                                                          Permanent attacker,
                                                          List<Permanent> defenderBattlefield) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeBlockedIfDefenderControlsMatchingPermanentEffect restriction) {
                boolean defenderMatches = defenderBattlefield != null && defenderBattlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
                if (defenderMatches) {
                    return true;
                }
            }
        }
        return false;
    }

    static UUID findControllerOf(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf != null && bf.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    static UUID getEffectiveRecipient(GameData gameData, UUID playerId) {
        if (gameData.mindControlledPlayerId != null
                && gameData.mindControlledPlayerId.equals(playerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return playerId;
    }
}
