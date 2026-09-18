package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutTargetCardExiledWithSourceIntoOwnersHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardExiledWithSourceIntoOwnersHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        UUID targetId = entry.getTargetId();
        ExiledCardEntry exiled = targetId == null ? null : gameData.findExiledCard(targetId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        var effect = (PutTargetCardExiledWithSourceIntoOwnersHandEffect) effectToResolve;
        if (exiled == null || sourcePermanentId == null
                || !sourcePermanentId.equals(exiled.sourcePermanentId())
                || exiled.faceDown()
                || effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                exiled.card(), effect.filter(), entry.getCard().getId(), gameData, exiled.ownerId())) {
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            return;
        }
        gameData.addCardToHand(exiled.ownerId(), exiled.card());
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " is put into its owner's hand."));
    }
}
