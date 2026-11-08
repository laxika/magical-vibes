package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Removes the permanent linked to the source via {@link com.github.laxika.magicalvibes.model.Permanent#getChosenPermanentId()}.
 *
 * <p>Placed in "when this leaves the battlefield" and/or {@code Triggers.becomesUntapped()} (Coffin Queen exiles the
 * creature it reanimated on both). The handler reads the link as the ability resolves: from the source on the
 * battlefield (the becomes-untapped trigger), else from the stack entry's source as it last existed
 * ({@code StackEntry.getSourcePermanentSnapshot()}, the leaves-the-battlefield trigger), and removes that linked
 * permanent according to {@link Mode} (exile or sacrifice). The link is cleared, so a second untap is a no-op. Used by
 * Dance of Many for the mutual bond between the enchantment and its token: when the enchantment leaves it exiles the
 * token ({@code EXILE}); when the token leaves it sacrifices the enchantment ({@code SACRIFICE_PARTNER}).
 *
 * @param mode              how the linked permanent is removed
 * @param linkedPermanentId the linked permanent named by an engine-built delayed trigger (Coffin Queen's "when you lose
 *                          control of this creature"), or {@code null} for a card's own ability, which reads the link
 *                          from its source
 */
public record RemoveLinkedPermanentEffect(Mode mode, UUID linkedPermanentId) implements CardEffect {

    /** Card-authored form: the link is read from the source as the ability resolves. */
    public RemoveLinkedPermanentEffect(Mode mode) {
        this(mode, null);
    }

    public enum Mode {
        /** Exile the linked permanent (enchantment leaves → exile the token). */
        EXILE,
        /** Sacrifice the linked permanent (Tyrannical Pitlord: the leaving creature sacrifices its chosen creature). */
        SACRIFICE,
        /**
         * Sacrifice the linked permanent, where the ability belongs to the linked permanent itself (token leaves →
         * sacrifice the enchantment). The ability is therefore controlled by the linked permanent's controller.
         */
        SACRIFICE_PARTNER
    }
}
