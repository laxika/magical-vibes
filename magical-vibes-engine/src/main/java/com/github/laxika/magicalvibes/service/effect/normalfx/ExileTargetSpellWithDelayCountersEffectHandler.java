package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellWithDelayCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveDelayCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExileTargetSpellWithDelayCountersEffect} (Ertai's Meddling).
 *
 * <p>The target spell is moved from the stack to exile. This is not countering, so
 * "can't be countered" does not protect the spell. A snapshot of its stack entry is kept so the
 * spell can be put back with the same X value and targets once the delay counters run out.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetSpellWithDelayCountersEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetSpellWithDelayCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        // "X can't be 0" — a zero X can never be announced, so nothing is left to do here.
        int counters = entry.getXValue();
        if (counters <= 0) return;

        StackEntry target = gameData.stack.stream()
                .filter(se -> se.getTargetableId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            log.info("Game {} - Ertai's Meddling target no longer on stack", gameData.id);
            return;
        }

        StackEntry snapshot = new StackEntry(target);
        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (target.isCopy()) {
            // A copy of a spell is not a card; it ceases to exist instead of being exiled.
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " (a copy) ceases to exist."));
            return;
        }

        exileService.exileCard(gameData, target.getOwnerId(), target.getPhysicalCard());
        gameData.setExiledCardCounters(targetCardId, CounterType.DELAY, counters);
        // "At the beginning of each of that player's upkeeps, if that card is exiled, ...": a delayed triggered ability
        // of this spell, controlled by its controller (CR 603.7a, 603.7d), with an intervening "if" (CR 603.4). It has
        // no stated end, but it can do nothing once that card has left exile (CR 400.7), so it ends then.
        CardsStillInZone stillExiled = new CardsStillInZone(Zone.EXILE,
                gameData.exileEntryVersionsOf(List.of(targetCardId)));
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atEveryStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER),
                        TriggerOptions.DEFAULT.withInterveningIf(stillExiled),
                        new RemoveDelayCounterFromExiledSpellEffect(targetCardId, snapshot)),
                null, null, DelayedReferents.NONE, stillExiled).withWatchedPlayer(target.getControllerId()));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " is exiled with " + counters + " delay counter" + (counters == 1 ? "" : "s") + " on it."));
        log.info("Game {} - {} exiled with {} delay counters", gameData.id, target.getCard().getName(), counters);
    }
}
