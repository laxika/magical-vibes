package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingAndPutCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Looks at the top card privately and offers the matching-card reveal and counter placement as a
 * single may choice.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingAndPutCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingAndPutCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealMatchingAndPutCounterEffect typed =
                (LookAtTopCardMayRevealMatchingAndPutCounterEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();
        if (!predicateEvaluationService.matchesCardPredicate(
                topCard, typed.predicate(), entry.getCard().getId(), gameData, controllerId)) {
            log.info("Game {} - {} looks at non-matching top card ({})",
                    gameData.id, playerName, sourceName);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(typed),
                sourceName + " — Reveal " + topCard.getName()
                        + " and put a " + typed.counterType().name().toLowerCase()
                        + " counter on this permanent?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
    }
}
