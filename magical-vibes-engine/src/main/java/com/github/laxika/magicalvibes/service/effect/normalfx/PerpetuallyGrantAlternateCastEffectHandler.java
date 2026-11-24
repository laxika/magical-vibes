package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAlternateCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PerpetuallyGrantAlternateCastEffect} into the card store {@code GameData.perpetualAlternateCasts},
 * read as each card is cast ({@code GameQueryService.findGrantedAlternateCast}); logged as {@link PerpetualChangeLog}
 * says.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantAlternateCastEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAlternateCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantAlternateCastEffect) effect;
        var located = subjectResolutionService.locatedCards(gameData, entry, effect, grant.cards());
        located.forEach(card -> gameData.grantPerpetualAlternateCast(card.card().getId(), grant.grant()));
        PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, effect, grant.cards(),
                located, " perpetually gains an ability.");
    }
}
