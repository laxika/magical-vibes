package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersWhenUntapLockEndsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Runs the {@link RemoveCountersWhenUntapLockEndsEffect} cleanup for a permanent whose
 * {@code WHILE_SOURCE_TAPPED} untap locks have just ended — Giant Oyster's "When this creature
 * leaves the battlefield or becomes untapped, remove all -1/-1 counters from the creature."
 *
 * <p>Called from the two events that end such a lock: the untap paths
 * ({@code TapUntapSupport.untapPermanent}, {@code MayMiscHandlerService.handleMayNotUntapChoice})
 * and the single battlefield-removal cleanup point in {@link PermanentRemovalService}. Permanents
 * without the marker are untouched, so it is cheap to call unconditionally.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UntapLockReleaseService {

    private final GameLogService gameLogService;

    /**
     * Releases every untap lock {@code source} holds and, if the source carries a
     * {@link RemoveCountersWhenUntapLockEndsEffect}, removes all counters of that type from the
     * permanents that were locked.
     */
    public void releaseUntapLocks(GameData gameData, Permanent source) {
        List<CounterType> counterTypes = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(RemoveCountersWhenUntapLockEndsEffect.class::isInstance)
                .map(e -> ((RemoveCountersWhenUntapLockEndsEffect) e).counterType())
                .toList();
        if (counterTypes.isEmpty()) {
            return;
        }

        UUID sourceId = source.getId();
        List<Permanent> locked = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getUntapPreventedByPermanentIds().remove(sourceId)) {
                locked.add(p);
            }
        });

        for (Permanent p : locked) {
            for (CounterType counterType : counterTypes) {
                int removed = p.getCounterCount(counterType);
                if (removed <= 0) {
                    continue;
                }
                p.setCounterCount(counterType, 0);
                if (counterType == CounterType.OIL) {
                    gameData.recordOilCounterRemoved(p, removed);
                }
                gameLogService.append(gameData, GameLog.cardTextCard(
                        source.getCard(), " removes all counters it placed on ", p.getCard(), "."));
                log.info("Game {} - {} removes {} {} counters from {}", gameData.id,
                        source.getCard().getName(), removed, counterType, p.getCard().getName());
            }
        }
    }
}
