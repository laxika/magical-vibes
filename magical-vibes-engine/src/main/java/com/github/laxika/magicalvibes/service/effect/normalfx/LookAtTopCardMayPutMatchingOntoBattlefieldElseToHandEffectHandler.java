package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Privately looks at the top card, offering a matching card for the battlefield and moving every
 * other outcome to hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect typed =
                (LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();
        if (predicateEvaluationService.matchesCardPredicate(topCard, typed.predicate(), entry.getCard().getId())) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(typed),
                    sourceName + " — Put " + topCard.getName() + " onto the battlefield?"
            ));
            return;
        }

        putTopCardIntoHand(gameData, controllerId, topCard, playerName, sourceName);
    }

    private void putTopCardIntoHand(GameData gameData, UUID controllerId, Card topCard,
                                     String playerName, String sourceName) {
        gameData.playerDecks.get(controllerId).removeFirst();
        gameData.addCardToHand(controllerId, topCard);
        gameLogService.append(gameData,
                GameLog.text(playerName + " puts the top card into their hand (" + sourceName + ")."));
        log.info("Game {} - {} puts {} into hand from library top ({})",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}
