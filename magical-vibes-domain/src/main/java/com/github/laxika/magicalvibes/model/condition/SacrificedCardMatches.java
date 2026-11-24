package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "If the sacrificed [permanent] was …": the permanent sacrificed to cast or activate the entry, or by its resolution
 * ("you may sacrifice another creature. If you do, …"), matched either on its card's characteristics ({@code filter})
 * or as the permanent last existed on the battlefield ({@code permanentFilter}: a designation such as suspected or
 * commander, CR 608.2h). Exactly one of the two filters is set.
 */
public record SacrificedCardMatches(CardPredicate filter, PermanentPredicate permanentFilter, String description)
        implements Condition {

    public SacrificedCardMatches {
        if ((filter == null) == (permanentFilter == null)) {
            throw new IllegalArgumentException("A sacrificed-permanent condition has a card filter or a permanent filter");
        }
    }

    /** The sacrificed permanent's card had the given characteristics. */
    public SacrificedCardMatches(CardPredicate filter, String description) {
        this(filter, null, description);
    }

    /** The sacrificed permanent, as it last existed on the battlefield, matched {@code permanentFilter}. */
    public static SacrificedCardMatches permanent(PermanentPredicate permanentFilter, String description) {
        return new SacrificedCardMatches(null, permanentFilter, description);
    }

    @Override
    public String conditionName() {
        return "sacrificed permanent was " + description;
    }

    @Override
    public String conditionNotMetReason() {
        return "the sacrificed permanent was not " + description;
    }
}
