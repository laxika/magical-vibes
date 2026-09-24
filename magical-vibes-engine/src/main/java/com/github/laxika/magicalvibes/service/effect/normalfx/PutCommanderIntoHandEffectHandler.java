package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCommanderIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCommanderIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCommanderIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> commandZone = gameData.playerCommandZones.get(controllerId);
        if (commandZone == null || commandZone.isEmpty()) {
            return;
        }

        if (commandZone.size() == 1) {
            putIntoHand(gameData, controllerId, commandZone.getFirst().getId());
            return;
        }

        playerInputService.beginCommandZoneCardChoice(
                gameData,
                controllerId,
                commandZone,
                "Choose a commander to put into your hand.");
    }

    public void completeChoice(GameData gameData, UUID playerId, UUID cardId) {
        putIntoHand(gameData, playerId, cardId);
    }

    private void putIntoHand(GameData gameData, UUID playerId, UUID cardId) {
        List<Card> commandZone = gameData.playerCommandZones.get(playerId);
        if (commandZone == null) {
            return;
        }
        Card commander = commandZone.stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
        if (commander == null) {
            return;
        }

        commandZone.remove(commander);
        gameData.addCardToHand(playerId, commander);
        gameLogService.append(gameData, GameLog.cardThen(
                commander, " is put into its owner's hand from the command zone."));
    }
}
