package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardExiledWithSourceIntoOwnersGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        ExiledCardEntry exiled = targetId == null ? null : gameData.findExiledCard(targetId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect putEffect =
                (PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect) effect;
        if (exiled == null || sourcePermanentId == null
                || !sourcePermanentId.equals(exiled.sourcePermanentId())
                || exiled.faceDown()
                || !matches(gameData, entry, putEffect, exiled)) {
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            return;
        }
        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " is put into its owner's graveyard."));
    }

    private boolean matches(GameData gameData, StackEntry entry,
                            PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect effect,
                            ExiledCardEntry exiled) {
        return (effect.filter() == null || predicateEvaluationService.matchesCardPredicate(
                exiled.card(), effect.filter(), entry.getCard().getId(), gameData, exiled.ownerId()))
                && (!effect.requiresManaValueEqualsX()
                || exiled.card().getManaValue() == entry.getXValue());
    }
}
