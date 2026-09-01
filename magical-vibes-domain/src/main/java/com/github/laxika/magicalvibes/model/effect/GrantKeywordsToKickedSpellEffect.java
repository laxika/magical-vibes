package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Cast-time marker for keywords a spell gains while it is on the stack when kicked.
 */
public record GrantKeywordsToKickedSpellEffect(Set<Keyword> keywords) implements CardEffect {

    public GrantKeywordsToKickedSpellEffect {
        keywords = Set.copyOf(keywords);
    }

    public GrantKeywordsToKickedSpellEffect(Keyword keyword) {
        this(Set.of(keyword));
    }
}
