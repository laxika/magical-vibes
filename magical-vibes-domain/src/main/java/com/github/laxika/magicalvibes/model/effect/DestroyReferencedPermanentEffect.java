package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys one permanent named by a {@link PermanentReference} rather than chosen as a target —
 * "destroy this Aura" ({@link PermanentReference#SOURCE}, Arachnus Web / Ice Cage / Aether Storm),
 * "destroy enchanted creature" ({@link PermanentReference#ATTACHED}, Yoke of the Damned, Pooling Venom) and
 * "destroy it" / "destroy that creature" for the permanent the trigger event was about
 * ({@link PermanentReference#TRIGGERING}, Suleiman's Legacy, Mortal Wound, Spinal Graft, Dwindle). The
 * permanent is found by {@code SubjectResolutionService}.
 *
 * <p>Never targets, so it never fizzles: if the referenced permanent is gone at resolution — the
 * source has left, the Aura is no longer attached, the triggering permanent has moved zone —
 * nothing happens. Set {@code cannotBeRegenerated} for "It can't be regenerated."
 *
 * @param reference           which permanent is destroyed
 * @param cannotBeRegenerated when {@code true} regeneration shields are ignored
 */
public record DestroyReferencedPermanentEffect(PermanentReference reference,
                                               boolean cannotBeRegenerated) implements CardEffect {

    /** Regeneration still applies. */
    public DestroyReferencedPermanentEffect(PermanentReference reference) {
        this(reference, false);
    }
}
