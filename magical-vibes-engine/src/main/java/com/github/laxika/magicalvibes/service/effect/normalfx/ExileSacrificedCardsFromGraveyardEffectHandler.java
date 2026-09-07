package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSacrificedCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExileSacrificedCardsFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSacrificedCardsFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> exiledCards = new ArrayList<>();
        for (UUID cardId : entry.getSacrificedCardIds()) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (card == null || ownerId == null) {
                continue;
            }
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
            exileService.exileCard(gameData, ownerId, card);
            exiledCards.add(card);
        }
        if (!exiledCards.isEmpty()) {
            String names = exiledCards.stream().map(Card::getName).collect(Collectors.joining(", "));
            gameLogService.append(gameData, GameLog.text(entry.getCard().getName() + " exiles " + names + "."));
        }
    }
}
