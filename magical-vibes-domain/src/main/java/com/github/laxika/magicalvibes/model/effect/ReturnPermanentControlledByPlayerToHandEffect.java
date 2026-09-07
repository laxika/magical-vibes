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
 * @param actingPlayerIsController when true, the resolving ability's controller is the acting
 *                                 player even when the stack entry has an unrelated target
 *                                 (Sparkcaster)
 * @param thenCondition when non-null, the selected permanent is checked against this predicate
 *                      before it is returned; a matching permanent causes {@code thenEffect} to
 *                      resolve after the return
 * @param thenEffect effect to resolve after a returned permanent matches {@code thenCondition}
 */
public record ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun,
                                                            boolean controllerChooses,
                                                            boolean actingPlayerIsController,
                                                            PermanentPredicate thenCondition,
                                                            CardEffect thenEffect)
        implements CombatDamageTriggerContextEffect {

    public ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun) {
        this(filter, noun, false, false, null, null);
    }

    public ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun,
                                                         boolean controllerChooses) {
        this(filter, noun, controllerChooses, false, null, null);
    }

    public ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun,
                                                         PermanentPredicate thenCondition,
                                                         CardEffect thenEffect) {
        this(filter, noun, false, false, thenCondition, thenEffect);
    }

    public static ReturnPermanentControlledByPlayerToHandEffect controller(PermanentPredicate filter,
                                                                            String noun) {
        return new ReturnPermanentControlledByPlayerToHandEffect(filter, noun, false, true, null, null);
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
