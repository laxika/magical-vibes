package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapSourceThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TapSourceThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapSourceThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TapSourceThenEffect tapThen = (TapSourceThenEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null || !tapUntapSupport.tapPermanent(gameData, source)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " taps itself."));

        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("TapSourceThenEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new QueueReflexiveAbilityEffect(tapThen.thenEffect())));
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        int directIndex = entry.getEffectsToResolve().indexOf(effect);
        if (directIndex >= 0) {
            return directIndex;
        }
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect parent = entry.getEffectsToResolve().get(i);
            if (parent instanceof ConditionalEffect conditional && conditional.wrapped() == effect) {
                return i;
            }
            if (parent instanceof MayEffect may
                    && (may.wrapped() == effect || may.elseEffect() == effect)) {
                return i;
            }
        }
        return -1;
    }
}
