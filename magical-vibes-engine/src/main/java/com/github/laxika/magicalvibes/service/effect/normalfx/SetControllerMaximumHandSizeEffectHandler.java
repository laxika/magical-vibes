package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * A resolved maximum hand size change ("for the rest of the game"): floated on each named player with no end, its
 * amount fixed as it resolves (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class SetControllerMaximumHandSizeEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetControllerMaximumHandSizeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var change = (SetControllerMaximumHandSizeEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        int value = amountEvaluationService.evaluate(gameData, change.amount(),
                AmountContext.forStackEntry(entry, source));
        SetControllerMaximumHandSizeEffect fixed = change.fixedAt(value);
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, change.players())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    fixed,
                    null,
                    playerId,
                    null,
                    Duration.PERMANENT,
                    0));
        }
    }
}
