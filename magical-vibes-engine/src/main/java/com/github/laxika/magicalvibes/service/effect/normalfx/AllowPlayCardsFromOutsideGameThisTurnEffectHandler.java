package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromOutsideGameThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AllowPlayCardsFromOutsideGameThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayCardsFromOutsideGameThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AllowPlayCardsFromOutsideGameThisTurnEffect permissionEffect =
                (AllowPlayCardsFromOutsideGameThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> sideboard = com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, controllerId);
        if (sideboard != null) {
            int granted = 0;
            for (Card card : List.copyOf(sideboard)) {
                if (granted >= permissionEffect.maxCards()) {
                    break;
                }
                if (gameData.outsideGamePlayPermissions.contains(card.getId())) {
                    continue;
                }
                if (permissionEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, permissionEffect.filter(), null, gameData, controllerId)) {
                    gameData.outsideGamePlayPermissions.add(card.getId());
                    granted++;
                }
            }
        }

        String description = permissionEffect.filter() == null ? "cards" : "matching cards";
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may play " + description + " from outside the game this turn."));
    }
}
