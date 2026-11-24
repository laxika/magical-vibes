package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PerpetuallyGrantAnyColorManaToCastEffect} into the card store
 * {@code GameData.perpetualAnyColorManaForCastCardIds}, read as each card is cast; logged as {@link PerpetualChangeLog}
 * says.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantAnyColorManaToCastEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAnyColorManaToCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantAnyColorManaToCastEffect) effect;
        var located = subjectResolutionService.locatedCards(gameData, entry, effect, grant.cards());
        located.forEach(card -> gameData.perpetualAnyColorManaForCastCardIds.add(card.card().getId()));
        PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, effect, grant.cards(),
                located, " perpetually gains an ability.");
    }
}
