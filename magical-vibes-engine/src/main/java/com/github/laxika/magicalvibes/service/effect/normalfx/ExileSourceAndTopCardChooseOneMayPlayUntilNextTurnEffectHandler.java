package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceCardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, sourceCardId);
        if (sourceCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} rejuvenation trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), sourceCardId);
            return;
        }

        UUID sourceOwnerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCardId);
        if (sourceOwnerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, sourceCardId);
        exileService.exileCard(gameData, sourceOwnerId, sourceCard);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is exiled."));

        UUID controllerId = entry.getControllerId();
        List<UUID> exiledIds = new ArrayList<>();
        exiledIds.add(sourceCardId);

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiledIds.add(topCard.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                    .card(topCard)
                    .text(" from the top of their library.")
                    .build());
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ExiledCardMayPlayChoice(controllerId, exiledIds));
    }
}
