package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantGetCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link CantGetCountersEffect}: each named permanent and each named player gets the record as a floating
 * effect for its duration, which {@code GameQueryService.cantHaveCounters} and
 * {@code GameQueryService.playerCantGetCounters} read. A "for as long as" duration that no longer holds as the effect
 * would begin (its source left or phased out) makes nothing (CR 611.2b).
 */
@Component
@RequiredArgsConstructor
public class CantGetCountersEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final DurationClock durationClock;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantGetCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var lock = (CantGetCountersEffect) effect;
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        if (lock.permanents() != null) {
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, lock.permanents())) {
                FloatingContinuousEffect floating = FloatingContinuousEffect.onPermanent(sourceName,
                        entry.getSourcePermanentId(), entry.getControllerId(), effect, permanent.getId(),
                        lock.duration());
                if (durationClock.holds(gameData, floating)) {
                    gameData.addFloatingEffect(floating);
                    gameLogService.append(gameData, GameLog.text(FaceDownVisibility.publicName(gameData, permanent)
                            + " can't have counters put on it (" + sourceName + ")."));
                }
            }
        }
        if (lock.players() != null) {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, lock.players())) {
                FloatingContinuousEffect floating = FloatingContinuousEffect.onPlayer(sourceName,
                        entry.getSourcePermanentId(), entry.getControllerId(), effect, playerId, lock.duration());
                if (durationClock.holds(gameData, floating)) {
                    gameData.addFloatingEffect(floating);
                    gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                            + " can't get counters (" + sourceName + ")."));
                }
            }
        }
    }
}
