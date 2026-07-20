package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveilEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SurveilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SurveilEffect e = (SurveilEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        // Surveil 0 (or fewer): no surveil event occurs.
        if (e.count() <= 0) {
            return;
        }

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + " surveil).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        // Surveil 2+ uses the top-of-library / graveyard split interaction (shared with scry).
        // Surveil 1 stays a single "put the top card into your graveyard?" may-ability.
        if (e.count() > 1) {
            int count = Math.min(e.count(), deck.size());
            List<Card> topCards = new ArrayList<>(deck.subList(0, count));
            deck.subList(0, count).clear();

            String logEntry = playerName + " surveils " + count + " (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} surveils {} ({})", gameData.id, playerName, count, sourceName);

            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.Scry(controllerId, topCards, true));
            return;
        }

        Card topCard = deck.getFirst();

        String logEntry = playerName + " surveils 1 (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} surveils 1, top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(e),
                sourceName + " — Put " + topCard.getName() + " into your graveyard?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
    
    }
}
