package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a self-bounce contingency and continues with its payload only after a successful bounce. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnSelfToHandThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSelfToHandThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnSelfToHandThenEffect bounceThen = (ReturnSelfToHandThenEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !permanentRemovalService.removePermanentToHand(gameData, source)) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                " is returned to its owner's hand."));
        log.info("Game {} - {} returned to hand", gameData.id, source.getCard().getName());

        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex < 0) {
            throw new IllegalStateException(
                    "ReturnSelfToHandThenEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(bounceThen.thenEffect()));
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
