package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
public class ExileTopCardsChooseOneMayPlayUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsChooseOneMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = amountEvaluationService.evaluate(gameData,
                ((ExileTopCardsChooseOneMayPlayUntilNextTurnEffect) effect).count(),
                AmountContext.forStackEntry(entry, source));
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<UUID> exiledIds = new ArrayList<>();
        String controllerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiledIds.add(topCard.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(controllerName + " exiles ").card(topCard)
                    .text(" from the top of their library.").build());
        }

        if (!exiledIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ExiledCardMayPlayChoice(controllerId, exiledIds));
            log.info("Game {} - {} chooses one card among {} exiled cards to play until next turn",
                    gameData.id, entry.getCard().getName(), exiledIds.size());
        }
    }
}
