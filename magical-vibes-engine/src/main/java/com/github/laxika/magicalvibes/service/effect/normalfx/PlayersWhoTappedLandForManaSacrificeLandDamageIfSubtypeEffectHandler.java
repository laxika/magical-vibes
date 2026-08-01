package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect}: each player who
 * tapped a land for mana this turn sacrifices a land (APNAP simultaneous), then the source deals
 * damage to each player who sacrificed a matching subtype land this way.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect) effect;

        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!gameData.playersWhoTappedLandForManaThisTurn.contains(playerId)) {
                continue;
            }
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<Permanent> lands = battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, p, new PermanentIsLandPredicate()))
                    .toList();

            if (lands.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData,
                        GameLog.text(playerName + " has no lands to sacrifice."));
                log.info("Game {} - {} has no lands to sacrifice for Desolation", gameData.id, playerName);
                continue;
            }

            if (lands.size() == 1) {
                autoSacrificeIds.add(lands.getFirst().getId());
            } else {
                List<UUID> landIds = lands.stream().map(Permanent::getId).toList();
                choosers.add(new PendingForcedSacrifice(playerId, 1, landIds));
            }
        }

        if (choosers.isEmpty()) {
            sacrificeThenDamageIfSubtype(gameData, entry, autoSacrificeIds, e.subtype(), e.damage());
            return;
        }

        beginNextChooser(gameData, choosers, autoSacrificeIds, e.subtype(), e.damage(), entry);
    }

    /**
     * Advances the Desolation-style APNAP sacrifice queue, or finishes with sacrifice + subtype
     * damage when no choosers remain. Shared by the effect resolve path and the multi-permanent
     * choice completion handler.
     */
    public void beginNextChooser(GameData gameData, List<PendingForcedSacrifice> choosers,
            List<UUID> accumulatedSacrificeIds, CardSubtype subtype, int damageAmount,
            StackEntry damageEntry) {
        if (choosers.isEmpty()) {
            sacrificeThenDamageIfSubtype(gameData, damageEntry, accumulatedSacrificeIds, subtype, damageAmount);
            return;
        }

        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remaining = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.ForcedSacrificeThenDamageIfSubtype(
                        next.playerId(), remaining, List.copyOf(accumulatedSacrificeIds),
                        subtype, damageAmount, damageEntry),
                "Choose " + next.count() + " land" + (next.count() > 1 ? "s" : "") + " to sacrifice.");
    }

    /**
     * Sacrifices every permanent in {@code sacrificeIds} simultaneously, then deals
     * {@code damageAmount} to each controller who sacrificed a permanent matching {@code subtype}.
     */
    public void sacrificeThenDamageIfSubtype(GameData gameData, StackEntry damageEntry,
            List<UUID> sacrificeIds, CardSubtype subtype, int damageAmount) {
        Set<UUID> playersToDamage = new HashSet<>();
        PermanentHasSubtypePredicate subtypePred = new PermanentHasSubtypePredicate(subtype);

        for (UUID permId : sacrificeIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
            if (controllerId != null
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, perm, subtypePred)) {
                playersToDamage.add(controllerId);
            }
            destructionSupport.sacrificeAndLog(gameData, perm, controllerId);
        }

        if (!playersToDamage.isEmpty() && !damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playersToDamage.contains(playerId)) {
                    continue;
                }
                int raw = gameQueryService.applyDamageMultiplier(gameData, damageAmount, damageEntry);
                damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, raw);
            }
            gameOutcomeService.checkWinCondition(gameData);
        }
    }
}
