package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers one artifact spell from the controller's hand or graveyard, cast by paying life equal
 * to its mana value instead of paying its mana cost.
 */
public record MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect()
        implements CardEffect {
}
