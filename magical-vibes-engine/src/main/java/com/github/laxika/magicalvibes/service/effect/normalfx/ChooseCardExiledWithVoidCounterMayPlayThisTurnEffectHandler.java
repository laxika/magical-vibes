package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardExiledWithVoidCounterMayPlayThisTurnEffect;
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
public class ChooseCardExiledWithVoidCounterMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardExiledWithVoidCounterMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> eligibleCardIds;
        synchronized (gameData.exiledCards) {
            eligibleCardIds = gameData.exiledCards.stream()
                    .filter(exiled -> !controllerId.equals(exiled.ownerId()))
                    .map(ExiledCardEntry::card)
                    .map(Card::getId)
                    .filter(gameData.exiledCardsWithVoidCounters::contains)
                    .toList();
        }
        if (eligibleCardIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no exiled opponent-owned cards with void counters."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ExiledCardMayPlayChoice(
                        controllerId, eligibleCardIds, true, false, true));
        log.info("Game {} - {} chooses an opponent-owned card with a void counter to play",
                gameData.id, entry.getCard().getName());
    }
}
