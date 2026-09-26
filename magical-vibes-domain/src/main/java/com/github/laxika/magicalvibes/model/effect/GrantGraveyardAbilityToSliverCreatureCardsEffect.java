package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

/** Static effect that grants a graveyard-activated ability to Sliver creature cards. */
public record GrantGraveyardAbilityToSliverCreatureCardsEffect(ActivatedAbility ability)
        implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        return ability;
    }

    @Override
    public boolean appliesTo(Card card) {
        return card != null
                && card.hasType(CardType.CREATURE)
                && card.getSubtypes().contains(CardSubtype.SLIVER);
    }
}
