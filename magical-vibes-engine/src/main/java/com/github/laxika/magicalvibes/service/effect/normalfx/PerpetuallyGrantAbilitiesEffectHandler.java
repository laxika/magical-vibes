package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link PerpetuallyGrantAbilitiesEffect}, logged as {@link PerpetualChangeLog} says. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantAbilitiesEffect) effect;
        var located = subjectResolutionService.locatedCards(gameData, entry, effect, grant.cards());
        for (var card : located) {
            grant.abilities().forEach(ability -> gameData.grantPerpetualAbility(card.card().getId(), ability));
        }
        PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, effect, grant.cards(),
                located, " perpetually gains an ability.");
    }
}
