package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.rules.EntryCausedTriggerEvent;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Triggers the "whenever a permanent entering causes a triggered ability to trigger" abilities of an
 * {@link EntryCausedTriggerEvent} (CR 603.2: Strict Proctor), of every player's permanents as the layers leave them
 * ({@link TriggerIndex}), each handed whole to the stack door after the ability that caused it. The record names that
 * ability as the triggering spell ("that ability"), its controller as the triggering player and its stack object as
 * the non-targeting referent, whatever the effects are.
 */
@Service
public class EntryCausedTriggerTriggers {

    private static final Trigger ENTERING_CAUSES_ABILITY_TO_TRIGGER = Triggers.permanentEnteringCausesAbilityToTrigger();

    private final EventAbilitySupport abilities;

    public EntryCausedTriggerTriggers(EventAbilitySupport abilities) {
        this.abilities = abilities;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, EntryCausedTriggerEvent event) {
        StackEntry triggeredAbility = event.triggeredAbility();
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : abilities.abilities(gameData, permanent,
                        ENTERING_CAUSES_ABILITY_TO_TRIGGER)) {
                    abilities.put(gameData, ability, permanent.getCard(), permanent, controllerId,
                            ConditionContext.forPermanent(permanent, controllerId), entry -> {
                                entry.setNonTargeting(true);
                                entry.setTargetId(triggeredAbility.getTargetableId());
                                entry.setTargetZone(Zone.STACK);
                                entry.setTriggeringCardId(triggeredAbility.getTargetableId());
                                entry.setTriggeringSpell(triggeredAbility);
                                entry.setTriggeringPlayerId(triggeredAbility.getControllerId());
                            });
                }
            }
        });
    }
}
