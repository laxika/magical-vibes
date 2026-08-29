package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Grants keywords to the spell that caused the surrounding spell-cast trigger.
 * The grant is carried by the spell's stack entry and transferred to the permanent it becomes.
 */
public record GrantKeywordsToCastSpellEffect(Set<Keyword> keywords) implements CardEffect {

    public GrantKeywordsToCastSpellEffect {
        keywords = Set.copyOf(keywords);
    }
}
