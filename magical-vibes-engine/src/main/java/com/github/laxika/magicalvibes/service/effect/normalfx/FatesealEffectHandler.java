package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FatesealEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
public class FatesealEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FatesealEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        FatesealEffect fateseal = (FatesealEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID libraryOwnerId = gameQueryService.getOpponentId(gameData, controllerId);
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, fateseal.count(),
                AmountContext.forStackEntry(entry, source)));
        if (amount == 0) {
            return;
        }

        int count = Math.min(amount, deck.size());
        if (count == 0) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " fateseals " + amount
                            + ", but the opponent's library is empty."));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.Scry(controllerId, topCards, false, libraryOwnerId, false));

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " fateseals " + count + " card(s) of "
                        + gameData.playerIdToName.get(libraryOwnerId) + "'s library."));
        log.info("Game {} - {} fateseals {} card(s) of {}'s library", gameData.id,
                gameData.playerIdToName.get(controllerId), count, gameData.playerIdToName.get(libraryOwnerId));
    }
}
