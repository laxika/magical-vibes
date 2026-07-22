package com.github.laxika.magicalvibes.model.effect;

/**
 * Madness triggered ability (CR 702.34b): offer to cast the discarded card from exile for its
 * madness cost. Declining puts it into its owner's graveyard.
 *
 * <p>{@code madnessCost} is snapshotted at discard time (native {@code MadnessCast} or a granted
 * cost from {@link MadnessGrantingEffect}) so the cast still works if a grant source leaves the
 * battlefield before this trigger resolves. When null, the handler falls back to the card's
 * {@code MadnessCast} option.
 */
public record MadnessMayCastEffect(String madnessCost) implements CardEffect {

    /** Resolve cost from the card's MadnessCast at resolution time. */
    public MadnessMayCastEffect() {
        this(null);
    }
}
