package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCommanderFromCommandZoneIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCommanderFromCommandZoneIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCommanderFromCommandZoneIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> commandZone = gameData.playerCommandZones.getOrDefault(controllerId, List.of());
        if (commandZone.isEmpty()) {
            return;
        }
        if (commandZone.size() > 1) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.CommanderChoice(controllerId, new ArrayList<>(commandZone)));
            return;
        }
        moveToHand(gameData, controllerId, commandZone.getFirst());
    }

    private void moveToHand(GameData gameData, UUID playerId, Card commander) {
        gameData.playerCommandZones.get(playerId).remove(commander);
        gameData.addCardToHand(playerId, commander);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " puts ", commander,
                " into their hand from the command zone."));
    }
}
