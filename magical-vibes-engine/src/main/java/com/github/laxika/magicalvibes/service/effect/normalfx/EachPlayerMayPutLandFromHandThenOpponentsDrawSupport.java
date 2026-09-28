package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutLandFromHandThenOpponentsDrawSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds,
                                   Map<UUID, UUID> chosenCardIdsByPlayer,
                                   UUID sourceControllerId, String cardName) {
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            UUID playerId = orderedPlayerIds.get(i);
            List<UUID> validCardIds = landCardIds(gameData, playerId);
            if (validCardIds.isEmpty()) {
                continue;
            }

            List<UUID> remainingPlayerIds = new ArrayList<>(orderedPlayerIds.subList(i + 1, orderedPlayerIds.size()));
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice(
                            playerId, validCardIds, remainingPlayerIds, chosenCardIdsByPlayer,
                            sourceControllerId, cardName));
            log.info("Game {} - Awaiting {} to choose a land for {}", gameData.id,
                    gameData.playerIdToName.get(playerId), cardName);
            return true;
        }

        finish(gameData, chosenCardIdsByPlayer, sourceControllerId, cardName);
        return false;
    }

    private List<UUID> landCardIds(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return List.of();
        }
        return hand.stream()
                .filter(card -> card.hasType(CardType.LAND))
                .map(Card::getId)
                .toList();
    }

    private void finish(GameData gameData, Map<UUID, UUID> chosenCardIdsByPlayer,
                        UUID sourceControllerId, String cardName) {
        List<BattlefieldEntryCard> chosenCards = new ArrayList<>();
        Set<UUID> playersWhoPutLand = new LinkedHashSet<>();
        for (Map.Entry<UUID, UUID> choice : chosenCardIdsByPlayer.entrySet()) {
            List<Card> hand = gameData.playerHands.get(choice.getKey());
            if (hand == null) {
                continue;
            }
            Card card = hand.stream()
                    .filter(candidate -> candidate.getId().equals(choice.getValue())
                            && candidate.hasType(CardType.LAND))
                    .findFirst().orElse(null);
            if (card == null) {
                continue;
            }
            chosenCards.add(new BattlefieldEntryCard(choice.getKey(), choice.getKey(), card,
                    Zone.HAND, null));
            playersWhoPutLand.add(choice.getKey());
        }

        if (!chosenCards.isEmpty()) {
            battlefieldEntryBatchSupport.begin(gameData, chosenCards);
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(sourceControllerId) && !playersWhoPutLand.contains(playerId)) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
            }
        }
    }
}
