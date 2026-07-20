package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LockTargetPermanentEffect} by stamping a {@link FloatingContinuousEffect} onto
 * the target creature. The floating effect carries the lock facts (read via
 * {@code PermanentLockEffect} by the combat and ability-activation services) and expires through
 * the standard duration machinery ({@code UNTIL_END_OF_TURN} at cleanup, {@code UNTIL_YOUR_NEXT_TURN}
 * at the ability controller's next turn start).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LockTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LockTargetPermanentEffect lock = (LockTargetPermanentEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            log.info("Game {} - lock ability fizzles, target left the battlefield", gameData.id);
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), lock, target.getId(), null, null, lock.duration(), 0));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " " + describe(lock) + "."));
        log.info("Game {} - {} locked ({})", gameData.id, target.getCard().getName(), describe(lock));
    }

    private String describe(LockTargetPermanentEffect lock) {
        if (lock.locksBlocking() || lock.locksActivatedAbilities()) {
            return "can't attack or block and its activated abilities can't be activated";
        }
        return "can't attack this turn";
    }
}
