package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old final-chapter slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 10). Each constant is named after the slot whose collector it stands for
 * and names the trigger its cards were converted to and the zone the collector reads them in. The collectors in
 * {@code TriggerCollectionService} still decide when the ability triggers and which event data it is bound to, as the
 * slot's did, until step 15 of the refactor replaces them with effects that read the event. They read the abilities in
 * the legacy form ({@code TriggeredAbility.legacyEffects()}): "triggers only once each turn" as the keyed
 * {@code OncePerTurnTriggerEffect} the dispatcher judges (step 14, run 14). A Saga's own chapter abilities are not read
 * through a watch: {@code SagaChapterService} and {@code StepTriggerService} read them by their chapter trigger, and
 * they take no options (CR 714.2b).
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

    @Override
    public List<CardEffect> effectsOf(Card card) {
        return card.getLegacyTriggerEffects(trigger, zone);
    }

    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        return card.getLegacyTriggerEffectRegistrations(trigger, zone);
    }
}
