package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomCardThenPutOntoBattlefieldIfMatchesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MillBottomCardThenPutOntoBattlefieldIfMatchesEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillBottomCardThenPutOntoBattlefieldIfMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillBottomCardThenPutOntoBattlefieldIfMatchesEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + "'s library is empty — "
                            + entry.getCard().getName() + "'s ability does nothing."));
            return;
        }

        Card bottomCard = library.removeLast();
        if (!graveyardService.addCardToGraveyard(gameData, controllerId, bottomCard, Zone.LIBRARY)) {
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " puts ", bottomCard,
                " from the bottom of their library into their graveyard."));

        boolean matches = predicateEvaluationService.matchesCardPredicate(
                bottomCard,
                e.filter(),
                entry.getCard().getId(),
                gameData,
                controllerId,
                entry.getSourcePermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue(),
                entry.getSourcePermanentSnapshot());
        if (matches) {
            graveyardReturnSupport.reanimateTargetedCard(gameData, controllerId, bottomCard);
        }
    }
}
