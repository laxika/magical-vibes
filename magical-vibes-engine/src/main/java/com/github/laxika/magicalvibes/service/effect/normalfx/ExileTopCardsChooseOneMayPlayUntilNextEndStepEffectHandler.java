package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExilePlayDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class ExileTopCardsChooseOneMayPlayUntilNextEndStepEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect exileEffect =
                (ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect) effect;
        if (exileEffect.count() <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<UUID> exiledIds = new ArrayList<>();
        String controllerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < exileEffect.count() && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiledIds.add(topCard.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(controllerName + " exiles ").card(topCard)
                    .text(" from the top of their library.").build());
        }

        if (!exiledIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ExiledCardMayPlayChoice(
                            controllerId, exiledIds, ExilePlayDuration.NEXT_END_STEP));
            log.info("Game {} - {} chooses one card among {} exiled cards to play until next end step",
                    gameData.id, entry.getCard().getName(), exiledIds.size());
        }
    }
}
