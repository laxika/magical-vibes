package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Perpetually grants keywords, and optionally a triggered ability, to a target creature. */
public record PerpetuallyGrantKeywordsToTargetCreatureEffect(
        Set<Keyword> keywords,
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility,
        PermanentPredicate filter) implements KeywordGrantingEffect {

    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords) {
        this(keywords, null, null, new PermanentControlledBySourceControllerPredicate());
    }

    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords,
                                                          PermanentPredicate filter) {
        this(keywords, null, null, filter);
    }

    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords,
                                                          EffectSlot triggeredAbilitySlot,
                                                          CardEffect triggeredAbility) {
        this(keywords, triggeredAbilitySlot, triggeredAbility,
                new PermanentControlledBySourceControllerPredicate());
    }

    public PerpetuallyGrantKeywordsToTargetCreatureEffect {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
        keywords = Set.copyOf(keywords);
        if ((triggeredAbilitySlot == null) != (triggeredAbility == null)) {
            throw new IllegalArgumentException("Triggered ability slot and effect must be provided together");
        }
        if (filter == null) {
            throw new IllegalArgumentException("A permanent filter is required");
        }
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter());
    }
}
