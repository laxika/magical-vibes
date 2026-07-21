package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ScryEffect e = (ScryEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int scryAmount = Math.max(0, amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source)));

        // 701.22b: If a player is instructed to scry 0, no scry event occurs.
        if (scryAmount == 0) {
            return;
        }

        int count = Math.min(scryAmount, deck.size());

        // 701.22d: Empty library — scry event still occurs (triggers would fire), but nothing to interact with.
        if (count == 0) {
            String logMsg = gameData.playerIdToName.get(controllerId) + " scries " + scryAmount
                    + " but their library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.Scry(controllerId, topCards));

        String logMsg = gameData.playerIdToName.get(controllerId) + " scries " + count + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} scries {}", gameData.id, gameData.playerIdToName.get(controllerId), count);
    }
}
