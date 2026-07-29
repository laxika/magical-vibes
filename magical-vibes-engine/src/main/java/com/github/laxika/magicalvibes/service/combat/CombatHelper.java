package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/**
 * Utility methods shared across combat sub-services, including the {@code combat.attack} and
 * {@code combat.block} sub-packages — hence public, but not intended for use outside
 * {@code service.combat}.
 */
public final class CombatHelper {

    private CombatHelper() {}

    public static boolean isCantAttackOrBlockUnlessEquipped(GameQueryService gameQueryService,
                                                            GameData gameData,
                                                            Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockUnlessEquippedEffect.class::isInstance)
                && !gameQueryService.isEquipped(gameData, creature);
    }

    public static boolean isCantBeBlockedDueToDefenderCondition(PredicateEvaluationService predicateEvaluationService,
                                                          GameData gameData,
                                                          Permanent attacker,
                                                          List<Permanent> defenderBattlefield) {
        boolean landwalkIgnored = isLandwalkIgnoredForBlocking(gameData);
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockabilityRestrictionEffect restriction) {
                PermanentPredicate defenderPredicate = restriction.unblockableIfDefenderControls();
                if (defenderPredicate == null
                        || (landwalkIgnored && restriction.unblockableIfDefenderControlsIsLandwalk())) {
                    continue;
                }
                if (defenderControls(predicateEvaluationService, gameData, defenderBattlefield, defenderPredicate)) {
                    return true;
                }
            }
        }
        if (landwalkIgnored) {
            return false;
        }
        // Until-end-of-turn defender-condition grants (Barbarian Guides' snow landwalk).
        for (PermanentPredicate predicate : attacker.getUnblockableIfDefenderControlsUntilEndOfTurn()) {
            if (defenderControls(predicateEvaluationService, gameData, defenderBattlefield, predicate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a permanent on the board switches landwalk off for blocking purposes (Staff of the
     * Ages). Landwalk abilities (CR 702.14a) are then ignored; all other evasion still applies.
     */
    public static boolean isLandwalkIgnoredForBlocking(GameData gameData) {
        boolean[] found = {false};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!found[0] && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(LandwalkIgnoredForBlockingEffect.class::isInstance)) {
                found[0] = true;
            }
        });
        return found[0];
    }

    private static boolean defenderControls(PredicateEvaluationService predicateEvaluationService,
                                            GameData gameData,
                                            List<Permanent> defenderBattlefield,
                                            PermanentPredicate predicate) {
        return defenderBattlefield != null && defenderBattlefield.stream()
                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, predicate));
    }

    public static boolean isCantBeBlockedDueToHistoricCast(GameQueryService gameQueryService,
                                                     GameData gameData, Permanent attacker) {
        boolean hasEffect = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect.class::isInstance);
        if (!hasEffect) return false;
        UUID controllerId = gameData.findControllerOf(attacker);
        return controllerId != null && gameQueryService.playerCastHistoricSpellThisTurn(gameData, controllerId);
    }

    public static boolean isCantBeBlockedDueToAttackingAlone(GameData gameData, Permanent attacker) {
        boolean hasEffect = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBeBlockedIfAttackingAloneEffect.class::isInstance);
        if (!hasEffect) return false;
        UUID controllerId = gameData.findControllerOf(attacker);
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream().filter(Permanent::isAttacking).count() == 1;
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
