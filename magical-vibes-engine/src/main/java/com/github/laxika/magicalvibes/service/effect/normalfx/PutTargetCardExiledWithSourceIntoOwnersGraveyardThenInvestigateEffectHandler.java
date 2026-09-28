package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    @Lazy
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        ExiledCardEntry exiled = targetId == null ? null : gameData.findExiledCard(targetId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        var putEffect = (PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect) effect;
        if (exiled == null || sourcePermanentId == null
                || !sourcePermanentId.equals(exiled.sourcePermanentId())
                || exiled.faceDown()
                || (putEffect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                        exiled.card(), putEffect.filter(), entry.getCard().getId(), gameData, exiled.ownerId()))) {
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            return;
        }
        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " is put into its owner's graveyard."));

        permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(),
                CreateTokenEffect.ofClueToken(1), entry.getCard().getSetCode());
        triggerCollectionService.checkInvestigateTriggers(gameData, entry.getControllerId());
    }
}
