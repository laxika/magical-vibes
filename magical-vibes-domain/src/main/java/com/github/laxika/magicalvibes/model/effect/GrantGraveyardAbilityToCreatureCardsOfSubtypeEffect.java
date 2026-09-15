package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect that grants a graveyard-activated ability to creature cards of a chosen subtype
 * in its controller's graveyard, such as Dregscape Sliver's unearth grant to Slivers.
 */
public record GrantGraveyardAbilityToCreatureCardsOfSubtypeEffect(
        ActivatedAbility ability,
        CardSubtype subtype
) implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        return ability;
    }

    @Override
    public boolean appliesTo(Card card) {
        return card != null
                && card.hasType(CardType.CREATURE)
                && subtype != null
                && card.getSubtypes().contains(subtype);
    }
}
