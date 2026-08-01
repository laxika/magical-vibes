package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EpicExperimentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpicExperimentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EpicExperimentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int x = entry.getXValue();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        List<UUID> exiledThisProcess = new ArrayList<>();
        for (int i = 0; i < x && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            exiledThisProcess.add(card.getId());
            gameLogService.append(gameData, GameLog.builder().text(playerName + " exiles ").card(card)
                    .text(" (" + sourceName + ").").build());
        }

        // Track every card exiled this way so uncast ones go to the graveyard after free-casting.
        gameData.pendingExileFreeCastRemainderToGraveyard.clear();
        gameData.pendingExileFreeCastRemainderToGraveyard.addAll(exiledThisProcess);

        List<UUID> castableSpellIds = new ArrayList<>();
        for (UUID cardId : exiledThisProcess) {
            Card exiled = gameData.findExiledCard(cardId).card();
            if (isInstantOrSorcery(exiled) && exiled.getManaValue() <= x) {
                castableSpellIds.add(cardId);
            }
        }

        if (castableSpellIds.isEmpty()) {
            exileFreeCastQueueSupport.putRemainderIntoOwnersGraveyards(gameData);
            log.info("Game {} - {} found no castable instants/sorceries (mana value {} or less); remainder to graveyard",
                    gameData.id, sourceName, x);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting cast choices for {} exiled instants/sorceries",
                gameData.id, sourceName, castableSpellIds.size());
    }

    private static boolean isInstantOrSorcery(Card card) {
        return card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY);
    }
}
