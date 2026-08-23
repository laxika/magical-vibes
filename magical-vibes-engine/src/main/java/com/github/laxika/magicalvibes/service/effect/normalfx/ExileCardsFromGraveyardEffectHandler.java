package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardsFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardsFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardsFromGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Exile targeted cards that are still in graveyards
        int exiledCount = 0;
        List<Card> exiledCards = new ArrayList<>();
        if (targetCardIds != null && !targetCardIds.isEmpty()) {
            List<String> exiledNames = new ArrayList<>();
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    exiledNames.add(card.getName());
                    exiledCards.add(card);
                    UUID sourcePermanentId = e.trackWithSource()
                            ? entry.getSourcePermanentId() : null;
                    graveyardReturnSupport.exileCardFromAnyGraveyard(
                            gameData, cardId, card, sourcePermanentId);
                }
            }
            exiledCount = exiledNames.size();
            if (!exiledNames.isEmpty()) {
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from graveyard.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} exiled {} cards from graveyards", gameData.id, playerName, exiledNames.size());
            }
        }

        // Gain life after exile — flat, or once per card actually exiled this way (Rysorian Badger).
        int lifeGain = e.lifeGainPerExiledCard() ? e.lifeGain() * exiledCount : e.lifeGain();
        if (lifeGain > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, lifeGain);
        }

        boolean conditionalRiderApplies = e.conditionalFilter() != null
                && exiledCards.stream().anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                        card, e.conditionalFilter(), entry.getCard().getId()));
        if (conditionalRiderApplies) {
            if (e.conditionalLifeGain() > 0) {
                lifeSupport.applyGainLife(gameData, controllerId, e.conditionalLifeGain());
            }
            if (e.conditionalLifeLossEachOpponent() > 0) {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (!playerId.equals(controllerId)) {
                        lifeSupport.applyLifeLoss(gameData, playerId,
                                e.conditionalLifeLossEachOpponent(), entry.getCard().getName());
                    }
                }
            }
        }

        // "If you do, this creature assigns no combat damage this turn" — only when something was
        // actually exiled, and only for the ability's source permanent (the unblocked attacker).
        if (e.assignNoCombatDamage() && exiledCount > 0 && entry.getSourcePermanentId() != null) {
            gameData.creaturesPreventedFromDealingCombatDamage.add(entry.getSourcePermanentId());
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " assigns no combat damage this turn."));
            log.info("Game {} - {} assigns no combat damage this turn", gameData.id, entry.getCard().getName());
        }
    }
}
