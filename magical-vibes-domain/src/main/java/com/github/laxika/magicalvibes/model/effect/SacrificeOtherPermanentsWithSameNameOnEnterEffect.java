package com.github.laxika.magicalvibes.model.effect;

/**
 * "If this permanent would enter, instead sacrifice each other permanent with the same name you
 * control, then put this permanent onto the battlefield." (Alliances "self legend rule" land
 * cycle, e.g. Sheltered Valley.)
 * <p>
 * Replacement effect (CR 614) registered in {@code EffectSlot.STATIC} and applied by
 * {@code BattlefieldEntryService} before the permanent is placed, so the entering permanent is
 * never itself among the sacrificed ones. The permanent always enters — only the other copies
 * leave.
 */
public record SacrificeOtherPermanentsWithSameNameOnEnterEffect() implements ReplacementEffect {
}
