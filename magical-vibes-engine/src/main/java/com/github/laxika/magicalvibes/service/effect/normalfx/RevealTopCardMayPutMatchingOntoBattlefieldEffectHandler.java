package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Reveals the top card of the active player's library and offers a matching card as an optional
 * battlefield entry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMayPutMatchingOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPutMatchingOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMayPutMatchingOntoBattlefieldEffect typed =
                (RevealTopCardMayPutMatchingOntoBattlefieldEffect) effect;
        UUID libraryOwnerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        String playerName = gameData.playerIdToName.get(libraryOwnerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());

        if (!predicateEvaluationService.matchesCardPredicate(topCard, typed.predicate(), entry.getCard().getId())) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(topCard)
                    .text(" remains on top of " + playerName + "'s library (" + sourceName + ").")
                    .build());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), libraryOwnerId, List.of(typed),
                sourceName + " — Put " + topCard.getName() + " onto the battlefield?"));
    }
}
