package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileDragonApproachAndSearchSupport {

    private static final int REQUIRED_GRAVEYARD_CARDS = 4;

    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final LibrarySearchSupport librarySearchSupport;
    private final PlayerInputService playerInputService;

    public void begin(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        List<Card> matchingCards = gameData.playerGraveyards
                .getOrDefault(controllerId, List.of()).stream()
                .filter(card -> cardName.equals(card.getName()))
                .toList();

        if (matchingCards.size() < REQUIRED_GRAVEYARD_CARDS) {
            return;
        }

        gameData.graveyardTargetOperation.entryType = null;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = false;
        gameData.graveyardTargetOperation.singleGraveyard = false;
        gameData.graveyardTargetOperation.effects = null;
        gameData.graveyardTargetOperation.resolutionTimeDragonApproachResume = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                controllerId,
                new ArrayList<>(matchingCards),
                REQUIRED_GRAVEYARD_CARDS,
                REQUIRED_GRAVEYARD_CARDS,
                "Choose exactly four cards named " + cardName + " from your graveyard.");
    }

    public boolean complete(GameData gameData, StackEntry entry, List<UUID> cardIds) {
        gameData.graveyardTargetOperation.resolutionTimeDragonApproachResume = false;

        if (cardIds == null || cardIds.size() != REQUIRED_GRAVEYARD_CARDS) {
            return false;
        }

        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        List<Card> selectedCards = new ArrayList<>();
        for (UUID cardId : cardIds) {
            Card selected = graveyard.stream()
                    .filter(card -> card.getId().equals(cardId))
                    .findFirst()
                    .orElse(null);
            if (selected == null || !cardName.equals(selected.getName())) {
                return false;
            }
            selectedCards.add(selected);
        }

        for (Card selected : selectedCards) {
            if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, selected.getId(), selected)) {
                return false;
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " exiles ", selected, " from their graveyard."));
        }

        entry.setExileInsteadOfGraveyard(true);
        return librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> card.hasType(CardType.CREATURE) && card.getSubtypes().contains(CardSubtype.DRAGON),
                "Dragon creature cards",
                "Search your library for a Dragon creature card and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD);
    }
}
