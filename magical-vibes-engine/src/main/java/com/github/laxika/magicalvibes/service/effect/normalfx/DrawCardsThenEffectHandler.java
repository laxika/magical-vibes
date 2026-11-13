package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DrawCardsThenEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final DrawCardEffectHandler drawCardEffectHandler;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardsThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardsThenEffect drawThen = (DrawCardsThenEffect) effect;
        int drawnBefore = entry.getDrawnCardIdsThisResolution().size();

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int requiredDraws = amountEvaluationService.evaluate(gameData, drawThen.drawnCardAmount(),
                AmountContext.forStackEntry(entry, source));

        drawCardEffectHandler.resolve(gameData, entry, drawThen.drawEffect());
        if (gameData.interaction.isAwaitingInput()
                || gameData.resolvingMayEffectFromStack
                || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (entry.getDrawnCardIdsThisResolution().size() - drawnBefore < requiredDraws) {
            return;
        }

        queueTargetedReflexiveAbility(gameData, entry, drawThen.thenEffect());
    }

    private void queueTargetedReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        // "When you do" is an ability of its own (CR 603.12): the stack door chooses its target as it goes on the stack.
        triggeredAbilityStackService.putMade(gameData, ReflexiveAbilitySupport.record(gameData, entry, List.of(thenEffect)));
    }

}
