package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetGraveyardCardAndSameNameFromZonesEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetGraveyardCardAndSameNameFromZonesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        UUID targetCardId = entry.getTargetId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        Card targetedCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetedCard == null) {
            return; // Target removed — fizzle already handled by StackResolutionService
        }

        String cardName = targetedCard.getName();
        UUID targetPlayerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);

        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Collect all matching cards across hand, graveyard, and library of the card's owner
        List<Card> matchingCards = new ArrayList<>();

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand != null) {
            matchingCards.addAll(hand.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            matchingCards.addAll(graveyard.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            matchingCards.addAll(library.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        if (matchingCards.isEmpty()) {
            // No matching cards — just shuffle library and resolve
            if (library != null) {
                java.util.Collections.shuffle(library);
            }

            String exileLog = controllerName + " exiles 0 cards named \"" + cardName + "\" from " + targetName
                    + "'s hand, graveyard, and library. " + targetName + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} found 0 cards named \"{}\" in {}'s zones", gameData.id, controllerName, cardName, targetName);
            return;
        }

        // Present matching cards for "any number" selection
        playerInputService.beginMultiZoneExileChoice(gameData, controllerId, matchingCards, targetPlayerId, cardName);
    
    }
}
