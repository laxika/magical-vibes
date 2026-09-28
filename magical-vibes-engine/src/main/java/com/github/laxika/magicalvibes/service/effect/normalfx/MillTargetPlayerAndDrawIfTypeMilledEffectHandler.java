package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndDrawIfTypeMilledEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndDrawIfTypeMilledEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndDrawIfTypeMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillTargetPlayerAndDrawIfTypeMilledEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        var source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int requestedCount = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source));
        int cardsToMill = Math.min(Math.max(0, requestedCount), deck == null ? 0 : deck.size());
        List<Card> preview = cardsToMill == 0 ? List.of() : new ArrayList<>(deck.subList(0, cardsToMill));

        if (cardsToMill > 0) {
            graveyardService.resolveMillPlayer(gameData, targetPlayerId, cardsToMill);
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        Set<Card> inGraveyard = graveyard == null ? Set.of() : new HashSet<>(graveyard);
        boolean matchingCardMilled = preview.stream()
                .anyMatch(card -> card.hasType(e.cardType()) && inGraveyard.contains(card));
        if (matchingCardMilled) {
            UUID controllerId = entry.getControllerId();
            drawService.resolveDrawCard(gameData, controllerId);
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " draws a card."));
        }

        log.info("Game {} - {} milled {} card(s), matching {} card milled: {}",
                gameData.id, entry.getCard().getName(), cardsToMill, e.cardType(), matchingCardMilled);
    }
}
