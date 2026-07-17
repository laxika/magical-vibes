package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Removes the permanent linked to the source via {@link com.github.laxika.magicalvibes.model.Permanent#getChosenPermanentId()}.
 *
 * <p>Placed in {@code ON_SELF_LEAVES_BATTLEFIELD}. The dedicated trigger collector reads the leaving
 * permanent's {@code chosenPermanentId} and bakes it into {@link #linkedPermanentId()}; the handler
 * then removes that linked permanent according to {@link Mode} (exile or sacrifice). The id is carried
 * on the effect (not the stack entry's {@code targetId}) so resolution does not validate it as an
 * on-battlefield spell target and fizzle it. Used by Dance of Many for the mutual bond between the
 * enchantment and its token: when the enchantment leaves it exiles the token ({@code EXILE}); when the
 * token leaves it sacrifices the enchantment ({@code SACRIFICE}).
 */
public record RemoveLinkedPermanentEffect(Mode mode, UUID linkedPermanentId) implements CardEffect {

    /** Card-authored form: the concrete linked id is baked in by the trigger collector at trigger time. */
    public RemoveLinkedPermanentEffect(Mode mode) {
        this(mode, null);
    }

    public enum Mode {
        /** Exile the linked permanent (enchantment leaves → exile the token). */
        EXILE,
        /** Sacrifice the linked permanent (token leaves → sacrifice the enchantment). */
        SACRIFICE
    }
}
