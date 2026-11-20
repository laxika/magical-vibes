package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the stack-based, source-linked {@link DoesntUntapEffect} variants ({@code target()} or {@code triggering()}
 * with a {@code WHILE_SOURCE_*} condition): a floating effect on the target permanent, with this record as its payload, that
 * keeps it tapped during its controller's untap step (CR 502.3) for as long as its source is controlled, remains on
 * the battlefield or remains tapped; the duration clock ends it as that stops (CR 611.2b).
 *
 * <p>The {@code source()}/{@code attached()} + {@code ALWAYS} variants are continuous static effects read
 * directly in {@code UntapStepService} and never reach this handler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoesntUntapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoesntUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DoesntUntapEffect doesntUntap = (DoesntUntapEffect) effect;

        List<UUID> targetIds = subjectResolutionService.permanentIds(gameData, entry, effect, doesntUntap.subject());
        if (targetIds.isEmpty()) {
            return;
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        switch (doesntUntap.condition()) {
            case WHILE_SOURCE_CONTROLLED -> {
                if (!entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, sourcePermanentId))) {
                    return;
                }
                for (UUID targetId : targetIds) {
                    if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                        gameData.addFloatingEffect(new FloatingContinuousEffect(
                                UUID.randomUUID(), entry.getCard().getName(), sourcePermanentId,
                                entry.getControllerId(), doesntUntap, targetId, null, null,
                                Duration.WHILE_SOURCE_CONTROLLED, 0));
                    }
                }
            }
            case WHILE_SOURCE_ON_BATTLEFIELD -> {
                // A source that already left never starts the duration (CR 611.2b).
                if (gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                    return;
                }
                for (UUID targetId : targetIds) {
                    Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                    if (target == null) {
                        continue;
                    }
                    // "For as long as [this] remains on the battlefield", whoever controls it (Somnophore).
                    gameData.addEffectOnPermanent(entry, doesntUntap, targetId, Duration.WHILE_SOURCE_REMAINS);
                    gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), " won't untap as long as you control ", entry.getCard(), "."));
                    log.info("Game {} - {} untap prevented while {} on battlefield", gameData.id, target.getCard().getName(), entry.getCard().getName());
                }
            }
            case WHILE_SOURCE_TAPPED -> {
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
                if (source == null || sourceSnapshot == null
                        || source.getUntapSequence() != sourceSnapshot.getUntapSequence()) {
                    return;
                }
                for (UUID targetId : targetIds) {
                    Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                    if (target == null) {
                        continue;
                    }
                    // "For as long as [this] remains tapped", whoever controls it (Rust Tick).
                    gameData.addEffectOnPermanent(entry, doesntUntap, targetId, Duration.WHILE_SOURCE_REMAINS_TAPPED);
                    gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), " won't untap as long as ", entry.getCard(), " remains tapped."));
                    log.info("Game {} - {} untap prevented while {} remains tapped", gameData.id, target.getCard().getName(), entry.getCard().getName());
                }
            }
            case ALWAYS -> {
                // source()/attached() + ALWAYS are static effects handled by UntapStepService; never resolved on the stack.
            }
        }
    }
}
