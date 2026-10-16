package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Grants keywords to the spell that caused the surrounding spell-cast trigger.
 * The grant is carried by the spell's stack entry and transferred to the permanent it becomes,
 * or consumed by spell-resolution logic when the granted keyword applies to a nonpermanent spell.
 *
 * <p>A {@code null} duration keeps the grant on the entering permanent as an entry grant. A
 * {@link GrantDuration#UNTIL_YOUR_NEXT_TURN} duration is carried separately and lasts until the
 * start of the controller's next turn (Jade Orb of Dragonkind's hexproof on a Dragon).</p>
 */
public record GrantKeywordsToCastSpellEffect(Set<Keyword> keywords, GrantDuration duration) implements CardEffect {

    public GrantKeywordsToCastSpellEffect {
        keywords = Set.copyOf(keywords);
    }

    public GrantKeywordsToCastSpellEffect(Set<Keyword> keywords) {
        this(keywords, null);
    }
}
