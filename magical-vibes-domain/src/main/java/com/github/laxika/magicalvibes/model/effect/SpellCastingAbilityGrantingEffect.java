package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** A static effect that grants a spell-casting ability to matching spells. */
public interface SpellCastingAbilityGrantingEffect extends CardEffect {

    CardPredicate filter();

    Keyword grantedAbility();
}
