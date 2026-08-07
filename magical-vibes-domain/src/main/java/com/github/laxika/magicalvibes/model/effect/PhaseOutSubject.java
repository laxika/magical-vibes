package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent a {@link PhaseOutEffect} phases out.
 *
 * <ul>
 *   <li>{@link #SOURCE} — the permanent the ability came from ("this creature phases out"; Mist
 *       Dragon, Crystal Golem, Vaporous Djinn). Read from the stack entry's
 *       {@code sourcePermanentId}; the effect targets nothing.</li>
 *   <li>{@link #TARGET} — a chosen permanent ("target permanent phases out"; Reality Ripple,
 *       Vision Charm). Read from {@code targetId}; this is the only value that declares a
 *       {@code TargetSpec}. Narrow the legal target with a {@code PermanentPredicateTargetFilter}
 *       on the card.</li>
 *   <li>{@link #ATTACHED} — the permanent the source Aura or Equipment is attached to ("enchanted
 *       creature phases out"; Vanishing). Read from the source permanent's {@code attachedTo};
 *       the effect targets nothing, because the Aura's own enchant clause already chose it.</li>
 * </ul>
 *
 * <p>In every case attachments follow the phased-out permanent indirectly (CR 702.26g) and the
 * permanent is removed from combat (CR 506.4); both live inside {@code PhasingService}.
 */
public enum PhaseOutSubject {
    SOURCE,
    TARGET,
    ATTACHED
}
