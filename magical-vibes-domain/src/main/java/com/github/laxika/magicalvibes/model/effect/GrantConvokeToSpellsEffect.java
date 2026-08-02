package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect: matching spells the controller casts have convoke. */
public record GrantConvokeToSpellsEffect(CardPredicate filter) implements SpellCastingAbilityGrantingEffect {

    @Override
    public Keyword grantedAbility() {
        return Keyword.CONVOKE;
    }
}
