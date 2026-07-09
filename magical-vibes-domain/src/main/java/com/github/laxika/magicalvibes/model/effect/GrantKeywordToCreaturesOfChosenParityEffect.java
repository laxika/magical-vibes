package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Static effect: each creature (any controller) whose mana value matches the source permanent's
 * chosen odd/even quality gains the given keyword(s). The chosen parity is read from the source
 * permanent at runtime via {@code Permanent.getChosenManaValueParity()}; while unchosen (null),
 * no keyword is granted. Used by Ashling's Prerogative ("Each creature with mana value of the
 * chosen quality has haste.").
 */
public record GrantKeywordToCreaturesOfChosenParityEffect(Set<Keyword> keywords) implements CardEffect {

    public GrantKeywordToCreaturesOfChosenParityEffect(Keyword keyword) {
        this(Set.of(keyword));
    }
}
