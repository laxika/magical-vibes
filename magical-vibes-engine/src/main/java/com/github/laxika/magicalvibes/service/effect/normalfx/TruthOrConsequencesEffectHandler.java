package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TruthOrConsequencesState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TruthOrConsequencesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Truth or Consequences' private votes and reveals them before applying the results. */
@Component
@RequiredArgsConstructor
public class TruthOrConsequencesEffectHandler implements NormalEffectHandlerBean {

    private static final int TRUTH = 0;
    private static final int CONSEQUENCES = 1;

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;
    private final VotingSupport votingSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TruthOrConsequencesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TruthOrConsequencesState state = gameData.truthOrConsequences;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.order.addAll(votingSupport.addAdditionalControllerVotes(
                    gameData, apnapPlayers(gameData), entry.getControllerId()));
            promptNextPlayer(gameData, cardName);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        state.choices.put(state.currentPlayerId, gameData.chosenXValue);
        state.voteChoices.add(gameData.chosenXValue);
        gameData.chosenXValue = null;
        state.index++;

        if (state.index < state.order.size()) {
            promptNextPlayer(gameData, cardName);
            return;
        }

        finish(gameData, state, entry, cardName);
    }

    private void promptNextPlayer(GameData gameData, String cardName) {
        TruthOrConsequencesState state = gameData.truthOrConsequences;
        UUID playerId = state.order.get(state.index);
        state.currentPlayerId = playerId;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, TRUTH, CONSEQUENCES,
                "Choose 0 for truth or 1 for consequences for " + cardName + ".",
                cardName));
    }

    private void finish(GameData gameData, TruthOrConsequencesState state,
                        StackEntry entry, String cardName) {
        int truthVotes = (int) state.voteChoices.stream().filter(value -> value == TRUTH).count();
        int consequencesVotes = state.voteChoices.size() - truthVotes;

        StringBuilder reveal = new StringBuilder("Players reveal their Truth or Consequences votes: ");
        for (int i = 0; i < state.order.size(); i++) {
            if (i > 0) {
                reveal.append(", ");
            }
            UUID playerId = state.order.get(i);
            reveal.append(gameData.playerIdToName.get(playerId))
                    .append(" votes ")
                    .append(state.voteChoices.get(i) == TRUTH ? "truth" : "consequences");
        }
        gameLogService.append(gameData, GameLog.text(reveal + "."));

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Truth or Consequences resolution is not resumable");
        }

        List<CardEffect> results = new ArrayList<>();
        if (truthVotes > 0) {
            results.add(new DrawCardEffect(truthVotes));
        }
        if (consequencesVotes > 0) {
            results.add(new DealDamageToRandomOpponentEffect(3 * consequencesVotes));
        }
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex + 1, results);
        state.reset();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
        rotated.addAll(players.subList(0, activeIndex));
        return rotated;
    }
}
