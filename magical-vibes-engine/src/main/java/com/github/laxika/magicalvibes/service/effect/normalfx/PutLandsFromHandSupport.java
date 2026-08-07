package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared logic for The Great Aurora's "each player may put any number of land cards from their hand
 * onto the battlefield" step. Players choose in APNAP order, one at a time: each chooser is prompted
 * via {@link PendingInteraction.PutLandsFromHandChoice}, and answering advances to the next
 * remaining player who has a land card in hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutLandsFromHandSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    /**
     * Begins the land-put choice for the first player in {@code orderedPlayerIds} who holds a land
     * card, deferring the rest as {@code remainingPlayerIds}. Returns {@code true} when an
     * interaction was begun (resolution must pause), {@code false} when nobody has a land in hand.
     */
    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds, String cardName) {
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            UUID playerId = orderedPlayerIds.get(i);
            List<UUID> landCardIds = landCardIds(gameData, playerId);
            if (landCardIds.isEmpty()) {
                continue;
            }
            List<UUID> remaining = new ArrayList<>(orderedPlayerIds.subList(i + 1, orderedPlayerIds.size()));
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.PutLandsFromHandChoice(playerId, landCardIds, remaining, cardName));
            log.info("Game {} - Awaiting {} to put any number of {} lands onto the battlefield ({})",
                    gameData.id, gameData.playerIdToName.get(playerId), landCardIds.size(), cardName);
            return true;
        }
        return false;
    }

    /** The ids of every land card currently in {@code playerId}'s hand. */
    public List<UUID> landCardIds(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return List.of();
        }
        return hand.stream().filter(card -> card.hasType(CardType.LAND)).map(Card::getId).toList();
    }

    /**
     * Puts the chosen land cards from {@code playerId}'s hand onto the battlefield untapped. They
     * enter simultaneously, so each one sees the others as already on the battlefield.
     */
    public void applyPutChoice(GameData gameData, UUID playerId, List<UUID> chosenCardIds, String cardName) {
        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        if (hand == null || chosenCardIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts no land cards onto the battlefield (" + cardName + ")."));
            return;
        }

        Set<UUID> chosen = new HashSet<>(chosenCardIds);
        List<Card> lands = new ArrayList<>();
        hand.removeIf(card -> {
            if (chosen.contains(card.getId())) {
                lands.add(card);
                return true;
            }
            return false;
        });

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> alreadyEntered = new ArrayList<>();
        for (Card land : lands) {
            Permanent permanent = new Permanent(land);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                    enterTappedTypes, List.copyOf(alreadyEntered));
            alreadyEntered.add(permanent);
        }

        gameLogService.append(gameData, GameLog.text(playerName + " puts " + lands.size()
                + (lands.size() == 1 ? " land" : " lands") + " from their hand onto the battlefield ("
                + cardName + ")."));
        log.info("Game {} - {} puts {} lands onto the battlefield ({})",
                gameData.id, playerName, lands.size(), cardName);
    }
}
