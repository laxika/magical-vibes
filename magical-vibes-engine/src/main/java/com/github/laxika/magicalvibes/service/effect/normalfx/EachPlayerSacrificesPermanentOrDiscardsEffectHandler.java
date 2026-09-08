package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.PlaguecrafterState;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Plaguecrafter's APNAP sacrifice and fallback-discard sequence. */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesPermanentOrDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesPermanentOrDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerSacrificesPermanentOrDiscardsEffect sacrificeOrDiscard =
                (EachPlayerSacrificesPermanentOrDiscardsEffect) effect;
        PlaguecrafterState state = gameData.plaguecrafter;

        if (state.completed) {
            state.reset();
            return;
        }

        if (state.sacrificeChoicesInProgress) {
            state.sacrificeChoicesInProgress = false;
            beginDiscardPhase(gameData, entry);
            return;
        }

        if (state.active) {
            return;
        }

        state.reset();
        state.active = true;
        state.sourceControllerId = entry.getControllerId();

        List<UUID> automaticSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        PermanentPredicate filter = sacrificeOrDiscard.filter();

        for (UUID playerId : apnapPlayers(gameData)) {
            List<UUID> matchingIds = matchingSacrificeIds(gameData, playerId, entry, filter);
            if (matchingIds.isEmpty()) {
                state.playersWhoCannotSacrifice.add(playerId);
            } else if (matchingIds.size() == 1) {
                automaticSacrificeIds.add(matchingIds.getFirst());
            } else {
                choosers.add(new PendingForcedSacrifice(playerId, 1, matchingIds));
            }
        }

        if (choosers.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, automaticSacrificeIds);
            beginDiscardPhase(gameData, entry);
            return;
        }

        state.sacrificeChoicesInProgress = true;
        destructionSupport.beginNextForcedSacrificeFromQueue(gameData, choosers, automaticSacrificeIds, true);
    }

    private List<UUID> matchingSacrificeIds(GameData gameData, UUID playerId, StackEntry entry,
            PermanentPredicate filter) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
            return List.of();
        }
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter));
    }

    private void beginDiscardPhase(GameData gameData, StackEntry entry) {
        PlaguecrafterState state = gameData.plaguecrafter;
        state.remainingDiscardPlayers.addAll(state.playersWhoCannotSacrifice);
        startNextDiscard(gameData, entry);
    }

    private void startNextDiscard(GameData gameData, StackEntry entry) {
        PlaguecrafterState state = gameData.plaguecrafter;
        while (!state.remainingDiscardPlayers.isEmpty()) {
            UUID playerId = state.remainingDiscardPlayers.removeFirst();
            if (gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()) {
                continue;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginDiscardChoice(gameData, playerId, 1,
                    DiscardFollowUp.plaguecrafter(List.copyOf(state.remainingDiscardPlayers)));
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>();
        if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
            players.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!players.contains(playerId) && gameData.playerIds.contains(playerId)) {
                players.add(playerId);
            }
        }
        return players;
    }
}
