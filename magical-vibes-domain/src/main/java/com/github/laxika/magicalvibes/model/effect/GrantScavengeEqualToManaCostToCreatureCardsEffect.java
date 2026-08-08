package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Static effect that gives every creature card in its controller's graveyard scavenge (CR 702.97a)
 * for a cost equal to that card's mana cost — Varolz, the Scar-Striped.
 *
 * <p>Passively scanned from the {@code STATIC} slot via the {@link GraveyardAbilityGrantingEffect}
 * capability; there is no resolver. Unlike a fixed grant, the ability is built per card because the
 * cost is the card's own mana cost. A card with no mana cost gets nothing.</p>
 */
public record GrantScavengeEqualToManaCostToCreatureCardsEffect() implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        if (card == null || card.getManaCost() == null || card.getManaCost().isBlank()) {
            return null;
        }
        return Card.scavengeAbility(card.getManaCost());
    }
}
