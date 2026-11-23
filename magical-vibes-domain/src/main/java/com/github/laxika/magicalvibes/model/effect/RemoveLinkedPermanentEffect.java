package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the permanent linked to the source via {@link com.github.laxika.magicalvibes.model.Permanent#getChosenPermanentId()}.
 *
 * <p>Placed in a card's own "when this leaves the battlefield" ability (Diabolic Servitude exiles the creature it
 * returned, Tyrannical Pitlord sacrifices its chosen creature). The handler reads the link as the ability resolves: from
 * the source on the battlefield, else from the stack entry's source as it last existed
 * ({@code StackEntry.getSourcePermanentSnapshot()}, the leaves-the-battlefield trigger), and removes that linked
 * permanent according to {@link Mode} (exile or sacrifice). The link is cleared, so a second trigger is a no-op.
 * {@code SACRIFICE_PARTNER} is a linked token's own "when this leaves the battlefield, sacrifice [its creator]".
 *
 * @param mode how the linked permanent is removed
 */
public record RemoveLinkedPermanentEffect(Mode mode) implements CardEffect {

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
