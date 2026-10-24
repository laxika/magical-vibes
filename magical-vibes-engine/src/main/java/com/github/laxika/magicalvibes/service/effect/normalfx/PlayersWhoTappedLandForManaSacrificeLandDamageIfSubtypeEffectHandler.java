package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ContinueAfterDepartureEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect}: each player who
 * tapped a land for mana this turn sacrifices a land (APNAP simultaneous), then the source deals
 * damage to each player who sacrificed a matching subtype land this way. The sacrifices are one event and a step
 * of their own, so every CR 616.1 question is asked before any land leaves; the damage reads who sacrificed what from
 * the departure's last-known information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffectHandler
        implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final DestructionSupport destructionSupport;
    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect) effect;

        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : apnapPlayers(gameData)) {
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
                    .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p))
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
            if (!autoSacrificeIds.isEmpty()) {
                stepDepartureSupport.departThenContinue(gameData, entry, effect,
                        DepartureBatch.of(StepDepartureSupport.sacrifices(autoSacrificeIds)), this);
            }
            return;
        }

        beginNextChooser(gameData, choosers, autoSacrificeIds, e.subtype(), e.damage(), entry);
    }

    /**
     * Advances the Desolation-style APNAP sacrifice queue, or, when no choosers remain, sacrifices the chosen lands
     * and then deals the subtype damage. Shared by the effect resolve path and the multi-permanent choice
     * completion handler.
     */
    public void beginNextChooser(GameData gameData, List<PendingForcedSacrifice> choosers,
            List<UUID> accumulatedSacrificeIds, CardSubtype subtype, int damageAmount,
            StackEntry damageEntry) {
        if (choosers.isEmpty()) {
            sacrificeAfterChoices(gameData, damageEntry, accumulatedSacrificeIds, subtype, damageAmount);
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
     * Once every player has chosen, sacrifices every permanent in {@code sacrificeIds} as one event, as the step the
     * parked resolution resumes with (so every CR 616.1 question is asked before any land leaves), followed by the
     * subtype damage as the next step. With no parked resolution they are sacrificed at once.
     */
    private void sacrificeAfterChoices(GameData gameData, StackEntry damageEntry, List<UUID> sacrificeIds,
            CardSubtype subtype, int damageAmount) {
        var effect = new PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect(subtype, damageAmount);
        if (stepDepartureSupport.departAfterChoice(gameData, StepDepartureSupport.sacrifices(sacrificeIds),
                List.of(new ContinueAfterDepartureEffect(effect)))) {
            return;
        }
        List<Permanent> permanents = sacrificeIds.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(Objects::nonNull)
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList();
        List<DepartureResult> results = destructionSupport.sacrificeTogether(gameData, permanents, null).results();
        damagePlayersWhoSacrificedSubtype(gameData, damageEntry, results, subtype, damageAmount);
    }

    /** Deals the damage to each player who sacrificed a land of the subtype in the departure just recorded. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect) effect;
        damagePlayersWhoSacrificedSubtype(gameData, entry, entry.getResolutionResults().departures(),
                e.subtype(), e.damage());
    }

    /**
     * Deals {@code damageAmount} to each player who sacrificed a permanent that had {@code subtype} immediately
     * before it left, by its last-known subtypes and controller (CR 608.2h).
     */
    private void damagePlayersWhoSacrificedSubtype(GameData gameData, StackEntry damageEntry,
            List<DepartureResult> results, CardSubtype subtype, int damageAmount) {
        Set<UUID> playersToDamage = new HashSet<>();
        for (DepartureResult result : results) {
            if (result.sacrificed() && result.lastKnownControllerId() != null
                    && result.lastKnownSubtypes().contains(subtype)) {
                playersToDamage.add(result.lastKnownControllerId());
            }
        }

        if (!playersToDamage.isEmpty() && !damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (playersToDamage.contains(playerId)) {
                    damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, damageAmount);
                }
            }
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> ordered = new ArrayList<>(players.subList(activeIndex, players.size()));
        ordered.addAll(players.subList(0, activeIndex));
        return ordered;
    }
}
