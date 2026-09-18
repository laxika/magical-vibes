package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseMayBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMatchingToHandElseMayBottomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMatchingToHandElseMayBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMatchingToHandElseMayBottomEffect revealEffect =
                (RevealTopCardMatchingToHandElseMayBottomEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").").build());

        if (predicateEvaluationService.matchesCardPredicate(
                topCard, revealEffect.matchPredicate(), null, gameData, controllerId)) {
            deck.removeFirst();
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard)
                    .text(" into their hand (" + sourceName + ").").build());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(revealEffect),
                sourceName + " \u2014 Put " + topCard.getName() + " on the bottom of your library?"));
        log.info("Game {} - {} may put revealed {} on the bottom via {}",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}
