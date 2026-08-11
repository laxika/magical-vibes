package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Records audience-scoped hand and library reveals without depending on networking or transport.
 *
 * <p>Even rules-public reveals use an explicit list of seated players. That keeps card identity
 * out of spectator, diagnostic, and unrelated subscriber surfaces while still delivering the
 * existing reveal message to every player in the game.
 */
@Component
@RequiredArgsConstructor
public class CardRevealService {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GameMutationCoordinator mutationCoordinator;

    public void lookAtOpponentHand(GameData gameData, UUID viewerId) {
        lookAtHand(gameData, viewerId, gameQueryService.getOpponentId(gameData, viewerId));
    }

    public void lookAtHand(GameData gameData, UUID viewerId, UUID subjectPlayerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(subjectPlayerId, List.of());
        String viewerName = gameData.playerIdToName.get(viewerId);
        String subjectName = gameData.playerIdToName.get(subjectPlayerId);

        String suffix = hand.isEmpty() ? " It is empty." : "";
        gameLogService.append(gameData,
                GameLog.text(viewerName + " looks at " + subjectName + "'s hand." + suffix));
        revealToPlayer(
                gameData, subjectPlayerId, GameEventFact.RevealZone.HAND, hand, viewerId);
    }

    public void revealHandToAllPlayers(GameData gameData, UUID subjectPlayerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(subjectPlayerId, List.of());
        String subjectName = gameData.playerIdToName.get(subjectPlayerId);

        if (hand.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(subjectName + " reveals their hand. It is empty."));
        } else {
            GameLog.Builder reveal = GameLog.builder().text(subjectName + " reveals their hand: ");
            appendCards(reveal, hand);
            gameLogService.append(gameData, reveal.text(".").build());
        }
        revealToAllPlayers(
                gameData, subjectPlayerId, GameEventFact.RevealZone.HAND, hand);
    }

    public void lookAtFaceDownPermanent(GameData gameData, UUID viewerId, Permanent permanent) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        String viewerName = gameData.playerIdToName.get(viewerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        gameLogService.append(gameData,
                GameLog.text(viewerName + " looks at a face-down creature controlled by " + controllerName + "."));
        revealToPlayer(gameData, controllerId, GameEventFact.RevealZone.PERMANENT,
                List.of(permanent.getCard()), viewerId);
    }

    public void revealToPlayer(
            GameData gameData,
            UUID subjectPlayerId,
            GameEventFact.RevealZone zone,
            List<Card> cards,
            UUID recipientId) {
        revealToPlayers(gameData, subjectPlayerId, zone, cards, Set.of(recipientId));
    }

    public void revealToAllPlayers(
            GameData gameData,
            UUID subjectPlayerId,
            GameEventFact.RevealZone zone,
            List<Card> cards) {
        revealToPlayers(gameData, subjectPlayerId, zone, cards, gameData.orderedPlayerIds);
    }

    public void revealToPlayers(
            GameData gameData,
            UUID subjectPlayerId,
            GameEventFact.RevealZone zone,
            List<Card> cards,
            Collection<UUID> recipientIds) {
        Set<UUID> recipients = Set.copyOf(recipientIds);
        if (recipients.isEmpty()) {
            return;
        }
        List<GameEventFact.CardSnapshot> snapshots = cards.stream()
                .map(card -> new GameEventFact.CardSnapshot(
                        card.getId(),
                        card.getName(),
                        card.getSetCode(),
                        card.getCollectorNumber()))
                .toList();
        mutationCoordinator.emit(
                gameData,
                new GameEventFact.PrivateReveal(
                        UUID.randomUUID(), subjectPlayerId, zone, snapshots),
                new GameEventAudience(GameEventAudience.Visibility.PRIVATE, recipients));
    }

    private static void appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }
}
