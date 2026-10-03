package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingManifoldInsightsChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifoldInsightsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Manifold Insights' sequential opponent choices. */
@Component
@RequiredArgsConstructor
public class ManifoldInsightsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifoldInsightsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, 10);
        if (result == null) {
            return;
        }

        List<Card> revealedCards = result.topCards();
        logReveal(gameData, result.playerName(), revealedCards, entry.getCard());
        gameData.queueInteraction(new PendingManifoldInsightsChoice(
                entry.getControllerId(), opponentsStartingAfterController(gameData, entry.getControllerId()),
                revealedCards));
        beginNextChoice(gameData);
    }

    /** Completes one opponent's pick and returns whether the whole effect has finished. */
    public boolean completeCardChoice(GameData gameData, List<UUID> selectedCardIds) {
        PendingManifoldInsightsChoice pending = gameData.pollPendingInteraction(
                PendingManifoldInsightsChoice.class);
        if (pending == null) {
            throw new IllegalStateException("No Manifold Insights choice is pending");
        }
        UUID selectedCardId = selectedCardIds.isEmpty() ? null : selectedCardIds.getFirst();
        Card selected = pending.remainingCards().stream()
                .filter(card -> card.getId().equals(selectedCardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid Manifold Insights card"));
        if (selected.hasType(CardType.LAND)) {
            throw new IllegalStateException("Manifold Insights requires a nonland card");
        }

        gameData.addCardToHand(pending.controllerId(), selected);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(pending.controllerId()) + " puts ", selected,
                " into their hand."));

        List<Card> remainingCards = new ArrayList<>(pending.remainingCards());
        remainingCards.remove(selected);
        gameData.queueInteraction(new PendingManifoldInsightsChoice(
                pending.controllerId(), pending.remainingOpponentIds(), remainingCards));
        return beginNextChoice(gameData);
    }

    private boolean beginNextChoice(GameData gameData) {
        PendingManifoldInsightsChoice pending = gameData.pollPendingInteraction(
                PendingManifoldInsightsChoice.class);
        if (pending == null) {
            throw new IllegalStateException("No Manifold Insights state is pending");
        }

        List<UUID> remainingOpponents = new ArrayList<>(pending.remainingOpponentIds());
        while (!remainingOpponents.isEmpty()) {
            UUID opponentId = remainingOpponents.removeFirst();
            List<UUID> validCardIds = pending.remainingCards().stream()
                    .filter(card -> !card.hasType(CardType.LAND))
                    .map(Card::getId)
                    .toList();
            if (validCardIds.isEmpty()) {
                break;
            }

            gameData.queueInteraction(new PendingManifoldInsightsChoice(
                    pending.controllerId(), remainingOpponents, pending.remainingCards()));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                    opponentId, pending.remainingCards(), validCardIds,
                    false, true, false, true, false, 0, null, 1,
                    "Choose a different nonland card to put into "
                            + gameData.playerIdToName.get(pending.controllerId()) + "'s hand.",
                    1, false));
            return false;
        }

        putOnBottomRandomly(gameData, pending.controllerId(), pending.remainingCards());
        return true;
    }

    private void putOnBottomRandomly(GameData gameData, UUID controllerId, List<Card> cards) {
        if (cards.isEmpty()) {
            return;
        }
        List<Card> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        gameData.playerDecks.get(controllerId).addAll(shuffled);
        gameLogService.append(gameData, GameLog.text(
                "The rest are put on the bottom of " + gameData.playerIdToName.get(controllerId)
                        + "'s library in a random order."));
    }

    private void logReveal(GameData gameData, String playerName, List<Card> cards, Card sourceCard) {
        GameLog.Builder builder = GameLog.builder().text(playerName + " reveals ");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        builder.text(" from the top of their library with ").card(sourceCard).text(".");
        gameLogService.append(gameData, builder.build());
    }

    private List<UUID> opponentsStartingAfterController(GameData gameData, UUID controllerId) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int controllerIndex = orderedPlayers.indexOf(controllerId);
        if (controllerIndex < 0 || orderedPlayers.isEmpty()) {
            return List.of();
        }

        List<UUID> opponents = new ArrayList<>();
        for (int offset = 1; offset <= orderedPlayers.size(); offset++) {
            UUID playerId = orderedPlayers.get((controllerIndex + offset) % orderedPlayers.size());
            if (!playerId.equals(controllerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
