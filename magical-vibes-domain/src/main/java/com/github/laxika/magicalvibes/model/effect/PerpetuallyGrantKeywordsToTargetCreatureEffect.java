package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Perpetually grants keywords, and optionally a triggered ability, to a target creature. */
public record PerpetuallyGrantKeywordsToTargetCreatureEffect(
        Set<Keyword> keywords,
        Ability grantedAbility,
        PermanentPredicate filter) implements KeywordGrantingEffect {

    public PerpetuallyGrantKeywordsToTargetCreatureEffect {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
        keywords = Set.copyOf(keywords);
        if (filter == null) {
            throw new IllegalArgumentException("A permanent filter is required");
        }
    }

    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords) {
        this(keywords, null, new PermanentControlledBySourceControllerPredicate());
    }

    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords,
                                                          PermanentPredicate filter) {
        this(keywords, null, filter);
    }

    /** Keywords and {@code grantedAbility} for a target creature its controller controls. */
    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords, Ability grantedAbility) {
        this(keywords, grantedAbility, new PermanentControlledBySourceControllerPredicate());
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter());
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
