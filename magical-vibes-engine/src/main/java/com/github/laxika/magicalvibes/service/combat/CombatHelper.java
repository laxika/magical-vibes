package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatCreatureLimitEffect;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
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

    public static boolean isCantBeBlockedDueToDefenderCondition(GameQueryService gameQueryService,
                                                          PredicateEvaluationService predicateEvaluationService,
                                                          GameData gameData,
                                                          Permanent attacker,
                                                          List<Permanent> defenderBattlefield) {
        boolean landwalkIgnored = isLandwalkIgnoredForBlocking(gameData);
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockabilityRestrictionEffect restriction
                    && hasDefenderCondition(restriction, landwalkIgnored)
                    && defenderControls(predicateEvaluationService, gameData, defenderBattlefield,
                    attacker, restriction.unblockableIfDefenderControls())) {
                return true;
            }
        }
        for (CardEffect effect : gameQueryService.computeStaticBonus(gameData, attacker).grantedEffects()) {
            if (effect instanceof BlockabilityRestrictionEffect restriction
                    && hasDefenderCondition(restriction, landwalkIgnored)
                    && defenderControls(predicateEvaluationService, gameData, defenderBattlefield,
                    attacker, restriction.unblockableIfDefenderControls())) {
                return true;
            }
        }
        final boolean[] result = {false};
        gameData.forEachPermanent((playerId, source) -> {
            if (result[0] || !source.isAttached() || !attacker.getId().equals(source.getAttachedTo())
                    || source.isAuraEffectsIgnoredThisTurn()) {
                return;
            }
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof BlockabilityRestrictionEffect restriction
                        && hasDefenderCondition(restriction, landwalkIgnored)
                    && defenderControls(predicateEvaluationService, gameData, defenderBattlefield,
                        source, restriction.unblockableIfDefenderControls())) {
                    result[0] = true;
                    return;
                }
            }
        });
        if (result[0]) {
            return true;
        }
        if (landwalkIgnored) {
            return false;
        }
        // Until-end-of-turn defender-condition grants (Barbarian Guides' snow landwalk).
        for (PermanentPredicate predicate : attacker.getUnblockableIfDefenderControlsUntilEndOfTurn()) {
            if (defenderControls(predicateEvaluationService, gameData, defenderBattlefield, attacker, predicate)) {
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
                                            Permanent source,
                                            PermanentPredicate predicate) {
        return defenderBattlefield != null && defenderBattlefield.stream()
                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(p, predicate,
                        FilterContext.of(gameData)
                                .withSourceCardId(source.getCard().getId())
                                .withSourcePermanentSnapshot(source)));
    }

    private static boolean hasDefenderCondition(BlockabilityRestrictionEffect restriction, boolean landwalkIgnored) {
        return restriction.unblockableIfDefenderControls() != null
                && !(landwalkIgnored && restriction.unblockableIfDefenderControlsIsLandwalk());
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

    /**
     * Returns the smallest static cap on the number of attackers in the current combat.
     */
    public static int getMaximumAttackers(GameData gameData) {
        return getMaximumCombatCreatures(gameData, true);
    }

    /**
     * Returns the smallest static cap on the number of distinct blockers in the current combat.
     */
    public static int getMaximumBlockers(GameData gameData) {
        return getMaximumCombatCreatures(gameData, false);
    }

    private static int getMaximumCombatCreatures(GameData gameData, boolean attackers) {
        int[] maximum = {Integer.MAX_VALUE};
        gameData.forEachPermanent((ignored, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CombatCreatureLimitEffect limit) {
                    int candidate = attackers ? limit.maxAttackers() : limit.maxBlockers();
                    maximum[0] = Math.min(maximum[0], candidate);
                }
            }
        });
        return maximum[0];
    }

}
