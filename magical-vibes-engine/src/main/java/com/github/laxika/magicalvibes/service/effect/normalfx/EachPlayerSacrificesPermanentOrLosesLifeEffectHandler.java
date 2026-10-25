package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EachPlayerSacrificeOrLoseLifeState;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an APNAP each-player choice between sacrificing a permanent and losing life. */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesPermanentOrLosesLifeEffectHandler
        implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final StepDepartureSupport stepDepartureSupport;

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

        // Every player has chosen: the sacrifices are one event, a step departure, so a CR 616.1 question ends the
        // step before any permanent leaves; the life loss follows once it is done.
        gameData.rerunCurrentEffectAfterInteraction = false;
        List<UUID> sacrificeIds = state.sacrificeIds.stream()
                .filter(permanentId -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                    return permanent != null && !gameQueryService.cantBeSacrificed(gameData, permanent);
                })
                .toList();
        stepDepartureSupport.departThenContinue(gameData, entry, effect,
                DepartureBatch.of(StepDepartureSupport.sacrifices(sacrificeIds)), this);
    }

    /**
     * The players who chose (or had) to lose life lose it after the sacrifices, as the next step (CR 608.2c), with the
     * amounts fixed now (CR 608.2h): the sacrifices can take away a replacement the Lich's Duel Mastery sacrifice that
     * replaces a loss would have, so that sacrifice asks its CR 616.1 questions in the loss step, before any life is
     * lost. The choices are cleared.
     */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeEffect = (EachPlayerSacrificesPermanentOrLosesLifeEffect) effect;
        EachPlayerSacrificeOrLoseLifeState state = gameData.eachPlayerSacrificeOrLoseLife;
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : state.lifeLossPlayerIds) {
            losses.put(playerId, lifeLoss(gameData, entry, sacrificeEffect, playerId));
        }
        state.reset();
        if (!losses.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(losses)));
        }
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
                permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent)
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
