package com.github.laxika.magicalvibes.model.effect;

/**
 * A floating effect on a phased-out permanent: it does not phase in during its controller's untap step while the
 * effect lasts ("phases out until the next planeswalk", "until this leaves the battlefield", "through the end of its
 * controller's next turn"). The permanent phased out directly, so it would otherwise phase in at that untap step
 * (CR 702.26a).
 *
 * @param phasesInWhenEnded whether the permanent phases in as the effect ends, rather than at its controller's next
 *                          untap step
 * @param tapsOnPhaseIn     whether the permanent is tapped as it phases in (Oubliette)
 */
public record PhasedOutHeldEffect(boolean phasesInWhenEnded, boolean tapsOnPhaseIn) implements CardEffect {
}
