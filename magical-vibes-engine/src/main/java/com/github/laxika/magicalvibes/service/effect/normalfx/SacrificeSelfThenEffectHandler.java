package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sacrifices the source permanent as a resolution step and then, only if it was sacrificed ("if you do"),
 * resolves the payload against the same stack entry. A CR 616.1 question about how the permanent leaves ends
 * the step, and the payload follows as the next step, gated on the recorded sacrifice
 * ({@link StepDepartureSupport#sacrificeThenIfSacrificed}).
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfThenEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfThenEffect) effect;

        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        if (!entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, self.getId()))
                || gameQueryService.cantBeSacrificed(gameData, self)) {
            return;
        }

        stepDepartureSupport.sacrificeThenIfSacrificed(gameData, entry, effect, self.getId(),
                e.reflexive() ? new QueueReflexiveAbilityEffect(e.thenEffect()) : e.thenEffect());
    }
}
