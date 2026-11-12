package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.Set;

/**
 * Watches a game's rules events and the triggered abilities its collectors put toward the stack. Only the
 * test fixtures' shadow recorder implements it, to compare the slot collectors with the trigger pipeline
 * while event families move from one to the other. A game has none by default, and simulation copies never
 * carry one.
 */
public interface RulesObserver {

    /** Where a collector put a triggered ability, or the question that leads to one. */
    enum Route {
        /** A {@code TRIGGERED_ABILITY} stack entry added to the stack. */
        STACK,
        /** A triggered ability held until the mana ability that triggered it finishes resolving. */
        MANA_ABILITY_QUEUE,
        /** A triggered ability held while a subgame is being played. */
        SUSPENDED_QUEUE,
        /**
         * A question asked with a {@code PendingMayAbility}: an entry's "you may" as it resolves, or an option of a
         * replacement effect, a cost or a resolving object. Never a triggered ability on its way to the stack.
         */
        MAY_ABILITY,
        /** A pending interaction, such as a trigger's target choice. */
        INTERACTION
    }

    /**
     * {@code item} (a stack entry, a pending may ability or a pending interaction) was put on {@code route} while a
     * slot collector was collecting {@code collecting}, or with {@code null} outside any collector (a trigger put
     * on the stack once its target was chosen, for example). Its effect is the registration the collector
     * dispatched, after its trigger-only wrappers; the item may hold a rewritten effect instead.
     */
    default void triggerQueued(Route route, Object item, GameData.CollectingTrigger collecting) {
    }

    /** The trigger pipeline took these batches out of the game's {@link RulesEventLog}. */
    default void eventsDrained(List<RulesEventBatch> batches) {
    }

    /**
     * The slots in shadow mode: for their abilities the trigger pipeline also works out what it would trigger,
     * and reports it to {@link #wouldTrigger}, while the slot collectors still put them on the stack.
     */
    default Set<EffectSlot> shadowedSlots() {
        return Set.of();
    }

    /**
     * The trigger pipeline would have put {@code effects}, an ability of {@code source} held in {@code slot}, on
     * the stack for a rules event, once per call.
     */
    default void wouldTrigger(EffectSlot slot, Card source, List<CardEffect> effects) {
    }
}
