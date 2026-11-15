package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.rules.SagaEvent;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Triggers the "whenever the final chapter ability of a Saga you control triggers / resolves" abilities of a
 * {@link SagaEvent} (CR 603.2): those of the Saga controller's permanents, as the layers leave them
 * ({@link TriggerIndex}), each handed whole to the stack door with its source as it was and the Saga's mana value as
 * the event's value.
 */
@Service
public class SagaTriggers {

    private static final Trigger FINAL_CHAPTER_TRIGGERS = Triggers.finalChapterAbilityTriggers();
    private static final Trigger FINAL_CHAPTER_RESOLVES = Triggers.finalChapterAbilityResolves();

    private final EventAbilitySupport abilities;

    public SagaTriggers(EventAbilitySupport abilities) {
        this.abilities = abilities;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, SagaEvent event) {
        Trigger watched = switch (event.kind()) {
            case FINAL_CHAPTER_TRIGGERED -> FINAL_CHAPTER_TRIGGERS;
            case FINAL_CHAPTER_RESOLVED -> FINAL_CHAPTER_RESOLVES;
        };
        for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(event.sagaControllerId(),
                List.of()))) {
            for (TriggeredAbility ability : abilities.abilities(gameData, permanent, watched)) {
                abilities.put(gameData, ability, permanent.getCard(), permanent, event.sagaControllerId(),
                        ConditionContext.forPermanent(permanent, event.sagaControllerId()),
                        EventAbilitySupport.eventRecord(permanent, null, null, event.sagaManaValue(), null, null));
            }
        }
    }
}
