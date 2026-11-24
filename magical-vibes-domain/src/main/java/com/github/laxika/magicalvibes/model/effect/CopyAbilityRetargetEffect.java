package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Marker effect carried by the "you may choose new targets for the copy" choice after an activated or triggered
 * ability is copied (CR 707.10c). {@code MayCopyHandlerService.handleCopyAbilityRetargetChoice} recomputes the legal
 * targets for the copy on the stack: from {@code ability} when the copy was made from a known activated ability (Rings
 * of Brighthearth: its target filter, blocking restriction and modes), else from the copy's own effects and target
 * filter.
 *
 * @param ability the activated ability the copy was made from, or {@code null}
 */
public record CopyAbilityRetargetEffect(ActivatedAbility ability) implements CardEffect {

    /** Targets recomputed from the copy's own effects and target filter. */
    public CopyAbilityRetargetEffect() {
        this(null);
    }
}
