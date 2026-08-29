package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect that grants a graveyard-activated ability to every artifact card in its
 * controller's graveyard, such as Mishra, Tamer of Mak Fawa's unearth ability.
 */
public record GrantGraveyardAbilityToArtifactCardsEffect(ActivatedAbility ability)
        implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        return ability;
    }

    @Override
    public boolean appliesTo(Card card) {
        return card != null && card.hasType(CardType.ARTIFACT);
    }
}
