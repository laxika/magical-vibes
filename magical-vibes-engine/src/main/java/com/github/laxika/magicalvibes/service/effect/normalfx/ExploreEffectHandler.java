package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        ExploreEffect exploreEffect = (ExploreEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + " explores)."));
            return;
        }

        Card topCard = deck.getFirst();

        // Reveal the top card to all players
        gameLogService.append(gameData, GameLog.textCardText(sourceName + " explores — " + playerName + " reveals ", topCard, "."));

        if (topCard.hasType(CardType.LAND)) {
            // Land — put into controller's hand
            deck.removeFirst();
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", topCard, " into their hand."));
            log.info("Game {} - {} explores, reveals land {} — to hand",
                    gameData.id, sourceName, topCard.getName());
            // Explore is complete — check for "whenever a creature you control explores" triggers
            triggerCollectionService.checkExploreTriggers(gameData, controllerId);
        } else {
            // Not a land — put a +1/+1 counter on the exploring creature
            UUID exploringPermanentId = exploreEffect.targeted()
                    ? entry.getTargetId()
                    : entry.getSourcePermanentId();
            Permanent source = exploringPermanentId != null
                    ? gameQueryService.findPermanentById(gameData, exploringPermanentId)
                    : null;
            if (source != null && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
                int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, 1);
                if (placed > 0) {
                    source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, source);
                    permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                            gameData, source, placed, controllerId);
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                            placed == 1 ? " gets a +1/+1 counter." : " gets " + placed + " +1/+1 counters."));
                }
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
