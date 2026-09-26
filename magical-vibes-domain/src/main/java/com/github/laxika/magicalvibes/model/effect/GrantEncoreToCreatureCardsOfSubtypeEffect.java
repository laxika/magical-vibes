package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Static effect that grants encore with a cost equal to each matching card's mana value.
 */
public record GrantEncoreToCreatureCardsOfSubtypeEffect(CardSubtype subtype)
        implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        String cost = "{" + card.getManaValue() + "}";
        return new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore " + cost,
                ActivationTimingRestriction.SORCERY_SPEED);
    }

    @Override
    public boolean appliesTo(Card card) {
        return card != null
                && card.hasType(CardType.CREATURE)
                && subtype != null
                && card.getSubtypes().contains(subtype);
    }
}
