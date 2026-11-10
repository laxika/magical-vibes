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
 * slot's did, until step 15 of the refactor replaces them with effects that read the event.
 * <p>
 * Every collector of these watches reads the abilities themselves, through
 * {@link SagaAbilityReader}: "triggers only once each turn" comes from the ability, and the legacy view gives none of
 * them, so {@link #effectsOf} and {@link #registrationsOf} throw. A Saga's own chapter abilities are not read through a
 * watch: {@code SagaChapterService} and {@code StepTriggerService} read them by their chapter trigger, and they take
 * no options (CR 715.2b).
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

    /** Throws: the collectors of a Saga watch read its abilities themselves ({@link SagaAbilityReader}). */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of a Saga watch read its abilities themselves ({@link SagaAbilityReader}). */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
