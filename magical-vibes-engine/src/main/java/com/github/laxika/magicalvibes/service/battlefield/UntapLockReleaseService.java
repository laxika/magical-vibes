package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedDrawStepTriggerWhileSourceTapped;
import com.github.laxika.magicalvibes.model.action.DelayedSourceUntapsOrLeavesTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ends what lasts "for as long as [a permanent] remains tapped" when that permanent untaps or leaves the battlefield,
 * and triggers the delayed "when [it] leaves the battlefield or becomes untapped" abilities it is watched by (Giant
 * Oyster, Tawnos's Coffin): its {@code WHILE_SOURCE_TAPPED} untap locks are released, its
 * {@link DelayedDrawStepTriggerWhileSourceTapped}s end, and each {@link DelayedSourceUntapsOrLeavesTrigger} watching it
 * goes on the stack, aimed at its creature without targeting it when it names one.
 *
 * <p>Called from the two events that end such a duration: the untap paths
 * ({@code TapUntapSupport.untapPermanent}, {@code MayMiscHandlerService.handleMayNotUntapChoice}) and the single
 * battlefield-removal cleanup point in {@link PermanentRemovalService}. A permanent nothing watches is untouched, so
 * it is cheap to call unconditionally.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UntapLockReleaseService {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    /**
     * Releases every untap lock {@code source} holds, ends its "for as long as it remains tapped" delayed triggers and
     * puts the delayed abilities that trigger when it untaps or leaves on the stack.
     */
    public void releaseUntapLocks(GameData gameData, Permanent source) {
        UUID sourceId = source.getId();
        gameData.forEachPermanent((playerId, p) -> p.getUntapPreventedByPermanentIds().remove(sourceId));
        gameData.clearDelayedActions(DelayedDrawStepTriggerWhileSourceTapped.class,
                delayed -> delayed.sourcePermanentId().equals(sourceId));

        List<DelayedSourceUntapsOrLeavesTrigger> triggered = gameData.drainDelayedActions(
                DelayedSourceUntapsOrLeavesTrigger.class, delayed -> delayed.sourcePermanentId().equals(sourceId));
        // Each delayed ability is a stack object of its own (CR 603.7a).
        for (DelayedSourceUntapsOrLeavesTrigger delayed : triggered) {
            UUID affectedId = delayed.affectedPermanentId();
            if (affectedId != null && gameQueryService.findPermanentById(gameData, affectedId) == null) {
                continue;
            }
            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(delayed.effect())), affectedId, sourceId);
            trigger.setNonTargeting(true);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} untaps-or-leaves delayed trigger", gameData.id, delayed.sourceCard().getName());
        }
    }
}
