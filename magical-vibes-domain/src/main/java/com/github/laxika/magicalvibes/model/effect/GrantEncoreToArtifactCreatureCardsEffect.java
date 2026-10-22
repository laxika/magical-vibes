package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Static effect that grants encore with a cost equal to each artifact creature card's mana cost (Phyrexian symbols included). A card
 * without a mana cost gets no encore ability, since its cost can't be paid.
 */
public record GrantEncoreToArtifactCreatureCardsEffect() implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        if (card == null || card.getManaCost() == null || card.getManaCost().isBlank()) {
            return null;
        }
        String cost = card.getManaCost();
        return new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore " + cost,
                ActivationTimingRestriction.SORCERY_SPEED);
    }

    @Override
    public boolean appliesTo(Card card) {
        return card != null
                && card.hasType(CardType.ARTIFACT)
                && card.hasType(CardType.CREATURE);
    }
}
