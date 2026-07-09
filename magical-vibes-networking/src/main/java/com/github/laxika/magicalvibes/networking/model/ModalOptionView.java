package com.github.laxika.magicalvibes.networking.model;

/**
 * One selectable mode of a modal ("choose one/two") spell or ETB trigger, as shown to the client.
 * {@code needsTarget} / {@code needsSpellTarget} tell the client whether picking this mode
 * requires selecting a permanent/player target or a spell-on-stack target before casting;
 * {@code targetCount} is the number of targets the mode declares (&gt;1 only for modes that
 * declare one filter per effect).
 */
public record ModalOptionView(String label, boolean needsTarget, boolean needsSpellTarget, int targetCount) {
}
