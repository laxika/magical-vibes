package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.rules.ControlChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Triggers the control-change abilities of a {@link ControlChangeEvent} (CR 603.2), each handed whole to the stack
 * door with the record of the event: "whenever an opponent gains control of a permanent you control" (where every
 * other player is an opponent, CR 806.1), read from the previous controller's permanents while the changing ones are
 * still among them, and the changed permanent's own "when you gain control of this from another player" and "when you
 * lose control of this". The abilities are read from {@link TriggerIndex} as the layers leave them; an ability's event
 * condition is judged here, its intervening "if" (CR 603.4) and limit (CR 603.2h) by the door.
 */
@Service
public class ControlChangeTriggers {

    private static final Trigger OPPONENT_GAINS_CONTROL = Triggers.opponentGainsControlOfPermanentYouControl();
    private static final Trigger YOU_GAIN_CONTROL = Triggers.youGainControlOfThis();
    private static final Trigger YOU_LOSE_CONTROL = Triggers.youLoseControlOfThis();

    private final EventAbilitySupport abilities;

    public ControlChangeTriggers(EventAbilitySupport abilities) {
        this.abilities = abilities;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, ControlChangeEvent event) {
        switch (event) {
            case ControlChangeEvent.OpponentGains gains -> opponentGains(gameData, gains);
            case ControlChangeEvent.Gained gained ->
                    ownControlChange(gameData, gained.permanent(), gained.controllerId(), YOU_GAIN_CONTROL);
            case ControlChangeEvent.Lost lost ->
                    ownControlChange(gameData, lost.permanent(), lost.controllerId(), YOU_LOSE_CONTROL);
        }
    }

    /**
     * The previous controller's "whenever an opponent gains control of a permanent you control" abilities: the record
     * names the new controller as the triggering player and the non-targeting "that player", the changed permanent as
     * the triggering permanent with its new controller, and the source as it was.
     */
    private void opponentGains(GameData gameData, ControlChangeEvent.OpponentGains event) {
        UUID previousControllerId = event.previousControllerId();
        UUID newControllerId = event.newControllerId();
        if (previousControllerId.equals(newControllerId)) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(previousControllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, OPPONENT_GAINS_CONTROL)) {
                abilities.put(gameData, ability, watcher.getCard(), watcher, previousControllerId,
                        ConditionContext.forPermanent(watcher, previousControllerId),
                        EventAbilitySupport.eventRecord(watcher, newControllerId, newControllerId, null,
                                event.permanent(), newControllerId));
            }
        }
    }

    /**
     * The changed permanent's own abilities watched as {@code watched}, controlled by {@code controllerId}, then the
     * delayed abilities about it that player controls: the record names the permanent as it was.
     */
    private void ownControlChange(GameData gameData, Permanent permanent, UUID controllerId, Trigger watched) {
        for (TriggeredAbility ability : abilities.abilities(gameData, permanent, watched)) {
            abilities.put(gameData, ability, permanent.getCard(), permanent, controllerId,
                    ConditionContext.forPermanent(permanent, controllerId),
                    EventAbilitySupport.eventRecord(permanent, null, null, null, null, null));
        }
        // The delayed abilities about it ("when you lose control of this creature, exile that creature", Coffin
        // Queen), whose "this" is that permanent (CR 603.7c) and whose "you" is their controller (CR 603.7e).
        for (Watcher delayed : abilities.delayedWatching(gameData, permanent.getId())) {
            if (!delayed.controllerId().equals(controllerId)) {
                continue;
            }
            for (TriggeredAbility ability : abilities.abilities(gameData, delayed, watched)) {
                abilities.put(gameData, ability, delayed, ConditionContext.forCard(delayed.card(), controllerId),
                        EventAbilitySupport.eventRecord(permanent, null, null, null, null, null));
            }
        }
    }
}
