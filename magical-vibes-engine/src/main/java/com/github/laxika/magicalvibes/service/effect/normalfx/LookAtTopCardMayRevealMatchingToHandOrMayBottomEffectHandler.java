package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandOrMayBottomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect typed =
                (LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect) effect;
        if (typed.stage() != Stage.LOOK) {
            return;
        }

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
        boolean matches = predicateEvaluationService.matchesCardPredicate(
                topCard, typed.predicate(), entry.getCard().getId(), gameData, controllerId);
        Stage nextStage = matches ? Stage.MAY_HAND : Stage.MAY_BOTTOM;
        String description = matches
                ? sourceName + " - Reveal " + topCard.getName() + " and put it into your hand?"
                : sourceName + " - Put " + topCard.getName() + " on the bottom of your library?";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(typed.withStage(nextStage)), description));
    }
}
