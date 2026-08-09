package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A player returns a permanent they control matching {@code filter} to its owner's hand. The acting
 * player is the one carried on the entry's {@code targetId} — "that player" (the caster) for
 * spell-cast triggers like Mana Breach — or, when no target is set, the resolving controller
 * (Kefnet the Mindful's "you may return a land you control", wrapped in a {@code MayEffect} to make
 * the return optional; Havengul Skaab's attack trigger). If that player controls no matching
 * permanent, nothing happens; otherwise they choose which one via the shared {@code BounceCreature}
 * choice context. {@code noun} names the permanent kind in the choice prompt ("land", "creature").
 *
 * <p>Exclude the source itself ("return <em>another</em> creature you control") with
 * {@code PermanentNotPredicate(new PermanentIsSourceCardPredicate())} inside the filter.</p>
 *
 * @param controllerChooses when true, the resolving ability's controller chooses a permanent
 *                          controlled by the acting player (Sigil of Sleep)
 */
public record ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun,
                                                            boolean controllerChooses)
        implements CombatDamageTriggerContextEffect {

    public ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun) {
        this(filter, noun, false);
    }

    /**
     * On a damage-to-player trigger, the player whose permanents are selectable is the damaged
     * player rather than the trigger controller.
     */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
