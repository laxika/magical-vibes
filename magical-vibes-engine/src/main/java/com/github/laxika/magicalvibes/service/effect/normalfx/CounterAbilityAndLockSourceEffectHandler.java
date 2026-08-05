package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterAbilityAndLockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CounterAbilityAndLockSourceEffect}: counters the targeted ability and stamps an
 * activated-ability lock onto the permanent that ability came from. The source permanent is read
 * before the entry is countered, because countering removes it from the stack.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterAbilityAndLockSourceEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterAbilityAndLockSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetId, entry);
        if (targetEntry == null) {
            return;
        }

        UUID sourcePermanentId = targetEntry.getSourcePermanentId();
        counterSupport.counterSpell(gameData, entry, targetEntry);
        lockSource(gameData, entry, (CounterAbilityAndLockSourceEffect) effect, sourcePermanentId);
    }

    private void lockSource(GameData gameData, StackEntry entry,
                            CounterAbilityAndLockSourceEffect effect, UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            log.info("Game {} - no lock applied, the ability's source permanent has left the battlefield",
                    gameData.id);
            return;
        }

        LockTargetPermanentEffect lock = new LockTargetPermanentEffect(
                false, false, true, effect.lockDuration(), TargetPredicates.permanent());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), lock, source.getId(), null, null, lock.duration(), 0));

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                "'s activated abilities can't be activated this turn."));
        log.info("Game {} - {} can't activate abilities for {}", gameData.id,
                source.getCard().getName(), effect.lockDuration());
    }
}
