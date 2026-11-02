package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Which of the old final-chapter slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 10). Each constant is named after the slot whose collector it stands for
 * and names the trigger its cards were converted to and the zone the collector reads them in. The collectors in
 * {@code TriggerCollectionService} still decide when the ability triggers and which event data it is bound to, as the
 * slot's did, until steps 14 and 15 of the refactor replace them. A Saga's own chapter abilities are not read through
 * a watch: {@code SagaChapterService} and {@code StepTriggerService} read them by their chapter trigger.
 */
public enum SagaWatch implements TriggerWatch {
    /** "Whenever the final chapter ability of a Saga you control resolves". */
    ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES(Triggers.finalChapterAbilityResolves(), Zone.BATTLEFIELD),
    /** "Whenever the final chapter ability of a Saga you control triggers". */
    ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS(Triggers.finalChapterAbilityTriggers(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    SagaWatch(Trigger trigger, Zone zone) {
        this.trigger = trigger;
        this.zone = zone;
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }

    @Override
    public Zone zone() {
        return zone;
    }
}
