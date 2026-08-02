package com.github.laxika.magicalvibes.model.effect;

/**
 * Intuition. Search your library for three cards and reveal them; the targeted opponent chooses
 * one of them, that card goes into the controller's hand and the rest into their graveyard, then
 * the library is shuffled. The controller picks the three cards through a
 * {@code PendingInteraction.IntuitionSearchChoice}; the opponent's pick rides the shared
 * {@code LIBRARY_REVEAL_CHOICE} prompt carried by {@code PendingIntuitionRevealChoice}.
 */
public record IntuitionEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
