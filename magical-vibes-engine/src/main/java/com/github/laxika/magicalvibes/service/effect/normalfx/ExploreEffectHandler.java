package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExploreEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + "'s library is empty (" + sourceName + " explores).");
            return;
        }

        Card topCard = deck.getFirst();

        // Reveal the top card to all players
        gameBroadcastService.logAndBroadcast(gameData,
                sourceName + " explores — " + playerName + " reveals " + topCard.getName() + ".");

        if (topCard.hasType(CardType.LAND)) {
            // Land — put into controller's hand
            deck.removeFirst();
            gameData.addCardToHand(controllerId, topCard);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + topCard.getName() + " into their hand.");
            log.info("Game {} - {} explores, reveals land {} — to hand",
                    gameData.id, sourceName, topCard.getName());
            // Explore is complete — check for "whenever a creature you control explores" triggers
            triggerCollectionService.checkExploreTriggers(gameData, controllerId);
        } else {
            // Not a land — put a +1/+1 counter on the exploring creature
            Permanent source = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            if (source != null && !gameQueryService.cantHaveCounters(gameData, source)) {
                source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + 1);
                gameBroadcastService.logAndBroadcast(gameData,
                        source.getCard().getName() + " gets a +1/+1 counter.");
            }

            // Ask: put the revealed card into your graveyard?
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId,
                    List.of(new ExploreEffect()),
                    sourceName + " — Put " + topCard.getName() + " into your graveyard?"
            ));

            log.info("Game {} - {} explores, reveals non-land {} — +1/+1 counter, may graveyard",
                    gameData.id, sourceName, topCard.getName());
        }
    
    }
}
