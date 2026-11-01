package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;

/**
 * Dredge N (CR 702.52a): a static ability of a card in a graveyard that may replace a draw by milling
 * {@code millCount} cards and returning that card to its owner's hand. Read by {@code DredgeSupport}.
 */
public record DredgeEffect(int millCount) implements CardEffect {

    /** Dredge functions only while the card is in a graveyard (CR 702.52a). */
    @Override
    public AbilityZones staticAbilityZones() {
        return AbilityZones.GRAVEYARD;
    }
}
