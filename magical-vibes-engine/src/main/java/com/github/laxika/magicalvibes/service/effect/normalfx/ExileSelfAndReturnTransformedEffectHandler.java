package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSelfAndReturnTransformedEffectHandler implements NormalEffectHandlerBean {

    private final ExileAndReturnTransformedService exileAndReturnTransformedService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndReturnTransformedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        boolean transformed =
                exileAndReturnTransformedService.exileAndReturnTransformed(gameData, entry.getSourcePermanentId());

        CardEffect thenEffect = ((ExileSelfAndReturnTransformedEffect) effect).thenEffect();
        if (transformed && thenEffect != null) {
            dispatch(gameData, entry, thenEffect);
        }
    }

    /**
     * Resolves the "if you do" payload through its own handler against this entry. {@link SequenceEffect}
     * has no handler of its own, so a multi-step payload is expanded here — dispatch is synchronous,
     * so its steps must be synchronous too, exactly as in {@code SacrificeSelfThenEffectHandler}.
     */
    private void dispatch(GameData gameData, StackEntry entry, CardEffect payload) {
        if (payload instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(payload);
        if (handler != null) {
            handler.resolve(gameData, entry, payload);
        } else {
            log.warn("No handler for payload effect in ExileSelfAndReturnTransformedEffect: {}",
                    payload.getClass().getSimpleName());
        }
    }
}
