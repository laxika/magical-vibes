package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Capability for static effects that grant hand-activated abilities to matching cards. */
public interface HandAbilityGrantingEffect extends CardEffect {

    /** Filter for cards in hands that receive the granted ability. */
    CardPredicate filter();

    /** Ability granted to every matching card in a hand. */
    ActivatedAbility grantedAbility();
}
