package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a hand-activated ability to matching cards in every player's hand. */
public record GrantHandActivatedAbilityToCardsEffect(ActivatedAbility grantedAbility,
                                                      CardPredicate filter)
        implements HandAbilityGrantingEffect {
}
