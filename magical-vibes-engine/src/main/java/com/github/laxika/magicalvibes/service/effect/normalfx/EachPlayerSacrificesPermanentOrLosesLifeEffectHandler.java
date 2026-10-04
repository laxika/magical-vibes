package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EachPlayerSacrificeOrLoseLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an APNAP each-player choice between sacrificing a permanent and losing life. */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesPermanentOrLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesPermanentOrLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeEffect = (EachPlayerSacrificesPermanentOrLosesLifeEffect) effect;
        EachPlayerSacrificeOrLoseLifeState state = gameData.eachPlayerSacrificeOrLoseLife;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(apnapPlayers(gameData, entry.getControllerId(), sacrificeEffect.opponentsOnly()));
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            applyChoice(gameData, entry, sacrificeEffect, state, chosenMode);
            return;
        }

        advance(gameData, entry, sacrificeEffect, state);
    }

    private void advance(GameData gameData, StackEntry entry,
            EachPlayerSacrificesPermanentOrLosesLifeEffect effect,
            EachPlayerSacrificeOrLoseLifeState state) {
        String sourceName = entry.getCard().getName();
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            state.currentPlayerId = playerId;

            List<UUID> matchingIds = matchingPermanentIds(gameData, entry, effect, playerId);
            if (matchingIds.isEmpty()) {
                state.lifeLossPlayerIds.add(playerId);
                continue;
            }

            if (!effect.mayChooseLife()) {
                if (matchingIds.size() == 1) {
                    state.sacrificeIds.add(matchingIds.getFirst());
                    continue;
                }
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginPermanentChoice(gameData, playerId, matchingIds,
                        new PermanentChoiceContext.SacrificeCreature(playerId),
                        "Choose " + effect.sacrificeDescription() + " to sacrifice.");
                return;
            }

            int lifeLoss = lifeLoss(gameData, entry, effect, playerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice(playerId, sourceName),
                    List.of(
                            ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice.sacrifice(
                                    effect.sacrificeDescription()),
                            ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice.loseLife(lifeLoss)
                    ),
                    sourceName + " — sacrifice " + effect.sacrificeDescription()
                            + " or lose " + lifeLoss + " life."));
            return;
        }

        destructionSupport.performSimultaneousSacrifice(gameData, state.sacrificeIds);
        for (UUID playerId : state.lifeLossPlayerIds) {
            lifeSupport.applyLifeLoss(gameData, playerId,
                    lifeLoss(gameData, entry, effect, playerId), sourceName);
        }
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void applyChoice(GameData gameData, StackEntry entry,
            EachPlayerSacrificesPermanentOrLosesLifeEffect effect,
            EachPlayerSacrificeOrLoseLifeState state, String chosenMode) {
        UUID playerId = state.currentPlayerId;
        if (ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice.sacrifice(
                effect.sacrificeDescription()).equals(chosenMode)) {
            List<UUID> matchingIds = matchingPermanentIds(gameData, entry, effect, playerId);
            if (matchingIds.isEmpty()) {
                state.lifeLossPlayerIds.add(playerId);
                advance(gameData, entry, effect, state);
            } else if (matchingIds.size() == 1) {
                state.sacrificeIds.add(matchingIds.getFirst());
                advance(gameData, entry, effect, state);
            } else {
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginPermanentChoice(gameData, playerId, matchingIds,
                        new PermanentChoiceContext.SacrificeCreature(playerId),
                        "Choose " + effect.sacrificeDescription() + " to sacrifice.");
            }
            return;
        }

        state.lifeLossPlayerIds.add(playerId);
        advance(gameData, entry, effect, state);
    }

    private List<UUID> matchingPermanentIds(GameData gameData, StackEntry entry,
            EachPlayerSacrificesPermanentOrLosesLifeEffect effect, UUID playerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
            return List.of();
        }
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                                gameData, permanent, effect.filter()));
    }

    private int lifeLoss(GameData gameData, StackEntry entry,
            EachPlayerSacrificesPermanentOrLosesLifeEffect effect, UUID playerId) {
        return amountEvaluationService.evaluate(gameData, effect.lifeLoss(),
                AmountContext.forStackEntry(entry, null)
                        .withControllerId(playerId)
                        .withTargetPermanentId(playerId));
    }

    private List<UUID> apnapPlayers(GameData gameData, UUID controllerId, boolean opponentsOnly) {
        List<UUID> players = new ArrayList<>();
        if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)
                && (!opponentsOnly || !gameData.activePlayerId.equals(controllerId))) {
            players.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!players.contains(playerId) && gameData.playerIds.contains(playerId)
                    && (!opponentsOnly || !playerId.equals(controllerId))) {
                players.add(playerId);
            }
        }
        return players;
    }
}
