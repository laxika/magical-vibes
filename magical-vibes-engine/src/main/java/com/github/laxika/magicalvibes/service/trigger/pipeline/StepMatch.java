package com.github.laxika.magicalvibes.service.trigger.pipeline;

import java.util.UUID;

/**
 * A step ability whose trigger condition a step or phase beginning met ({@link TriggerMatcher#matchedStep}).
 *
 * @param candidate    the ability and its source
 * @param group        how its source relates to the step, which orders it among the step's abilities and decides what
 *                     its record names
 * @param thatPlayerId the player "that player" names: the player whose step it is, or the enchanted player of a
 *                     Curse, or the controller of the permanent an Aura enchants
 * @param enchantedId  the permanent the source Aura enchants, for {@link StepGroup#ENCHANTED_CONTROLLER}, or
 *                     {@code null}
 */
record StepMatch(StepCandidate candidate, StepGroup group, UUID thatPlayerId, UUID enchantedId) {

    /**
     * How a step ability's source relates to the step: its zone and how the player whose step it is relates to the
     * ability's controller or to what the source is attached to.
     */
    enum StepGroup {
        /** A face-up plane's or phenomenon's ability, controlled by the planar controller (CR 901.6). */
        PLANE,
        /** An emblem's ability (CR 114.4). */
        EMBLEM,
        /** "Your" step, an ability of a permanent or face-up scheme. */
        YOU_BATTLEFIELD,
        /** "Your" step, an ability of a card in its owner's command zone. */
        YOU_COMMAND,
        /** "Your" step, an ability of a card in its owner's graveyard. */
        YOU_GRAVEYARD,
        /** "Each" step, an ability of a permanent or face-up scheme. */
        EACH_BATTLEFIELD,
        /** "Each opponent's" or "the chosen player's" step, an ability of a permanent or face-up scheme. */
        OPPONENT_BATTLEFIELD,
        /**
         * The step of the controller of the permanent the source Aura enchants, and "your" step of an Aura whose
         * ability acts on that player ("enchanted permanent's controller sacrifices …").
         */
        ENCHANTED_CONTROLLER,
        /** "Enchanted player's" step and "each" step of a Curse, which act on the enchanted player. */
        ENCHANTED_PLAYER,
        /**
         * "Your upkeep", the ability a card in exile has for the egg, refine or time counters on it
         * ({@link StepCandidate.ExileCounterAbility}).
         */
        EXILE_COUNTER,
        /** "Your" step, an ability of a card in exile. */
        YOU_EXILE,
        /** "Each" step, an ability of a suspended card in exile. */
        EACH_EXILE,
        /** "Each" or "each opponent's" step, an ability of a card in a command zone. */
        EACH_COMMAND,
        /** "Each" or "each opponent's" step, an ability of a card in a graveyard. */
        EACH_GRAVEYARD
    }
}
