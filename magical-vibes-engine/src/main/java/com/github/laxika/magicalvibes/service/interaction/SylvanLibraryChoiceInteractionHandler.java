package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles Sylvan Library's {@link PendingInteraction.SylvanLibraryChoice}: the player selects up to
 * {@code resolveCount} of their cards drawn this turn to put on top of their library; for each of
 * the remaining resolved cards they pay 4 life (forced to top a card instead when they cannot
 * afford it). Card views are re-derived from the player's current hand at prompt time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SylvanLibraryChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SylvanLibraryChoice> {

    private static final int LIFE_PER_CARD = 4;

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final LifeSupport lifeSupport;
    private final EffectResolutionService effectResolutionService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.SylvanLibraryChoice> handledType() {
        return PendingInteraction.SylvanLibraryChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.SylvanLibraryChoice interaction, UUID recipientId) {
        List<Card> hand = gameData.playerHands.getOrDefault(interaction.playerId(), List.of());
        List<UUID> ids = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        for (UUID id : interaction.drawnThisTurnCardIds()) {
            Card card = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if (card != null) {
                ids.add(id);
                cardViews.add(cardViewFactory.create(card));
            }
        }

        sessionManager.sendToPlayer(recipientId, InteractionPromptMessage.multiCardPick(ids, cardViews,
                interaction.resolveCount(),
                "Choose up to " + interaction.resolveCount() + " card(s) drawn this turn to put on top of your "
                        + "library. You pay 4 life for each of the " + interaction.resolveCount()
                        + " you don't put back."));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.SylvanLibraryChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        UUID playerId = interaction.playerId();
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }

        // Validate: only cards that were drawn this turn and are still in hand, unique, capped.
        List<UUID> toppedIds = new ArrayList<>();
        for (UUID id : chosenIds) {
            if (toppedIds.size() >= interaction.resolveCount()) {
                break;
            }
            boolean drawnThisTurn = interaction.drawnThisTurnCardIds().contains(id);
            boolean inHand = hand != null && hand.stream().anyMatch(c -> c.getId().equals(id));
            if (drawnThisTurn && inHand && !toppedIds.contains(id)) {
                toppedIds.add(id);
            }
        }

        gameData.interaction.clearAwaitingInput();

        // Put the chosen cards on top of the library (first chosen ends up nearest the top).
        List<Card> topped = new ArrayList<>();
        for (UUID id : toppedIds) {
            Card card = removeFromHand(hand, id);
            if (card != null) {
                topped.add(card);
            }
        }
        for (int i = topped.size() - 1; i >= 0; i--) {
            deck.addFirst(topped.get(i));
        }
        for (Card card : topped) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.textCardText(playerName + " puts ", card, " on top of their library (Sylvan Library)."));
        }

        // For each remaining resolved card, pay 4 life — or, if unaffordable, put a drawn-this-turn
        // card on top instead (the "pay 4 life or put on top" option that can be performed).
        int remaining = interaction.resolveCount() - topped.size();
        for (int i = 0; i < remaining; i++) {
            if (gameData.getLife(playerId) >= LIFE_PER_CARD) {
                lifeSupport.applyLifeLoss(gameData, playerId, LIFE_PER_CARD, "Sylvan Library");
            } else {
                Card forced = takeNextEligible(hand, interaction.drawnThisTurnCardIds());
                if (forced != null) {
                    deck.addFirst(forced);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " can't pay 4 life and puts ", forced, " on top of their library (Sylvan Library)."));
                }
            }
        }

        log.info("Game {} - Sylvan Library: {} put {} card(s) on top, resolved {} total",
                gameData.id, playerName, topped.size(), interaction.resolveCount());

        finishResolution(gameData);
    }

    private static Card removeFromHand(List<Card> hand, UUID id) {
        if (hand == null) {
            return null;
        }
        Card found = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        if (found != null) {
            hand.remove(found);
        }
        return found;
    }

    /** Removes and returns the next drawn-this-turn card still in hand (for the forced-top fallback). */
    private static Card takeNextEligible(List<Card> hand, List<UUID> drawnThisTurnCardIds) {
        if (hand == null) {
            return null;
        }
        for (UUID id : drawnThisTurnCardIds) {
            Card found = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if (found != null) {
                hand.remove(found);
                return found;
            }
        }
        return null;
    }

    private void finishResolution(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }
}
