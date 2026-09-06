package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardExiledWithSourceMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardExiledWithSourceMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardExiledWithSourceMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<UUID> exiledCardIds = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .map(card -> card.getId())
                .toList();
        if (exiledCardIds.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ExiledCardMayPlayChoice(controllerId, exiledCardIds, true));
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " chooses a card exiled with " + entry.getCard().getName()
                + " to play this turn."));
        log.info("Game {} - {} chooses a card exiled with {} to play this turn",
                gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
    }
}
