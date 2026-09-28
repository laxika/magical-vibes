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
        CardEffect triggeredAbility) implements KeywordGrantingEffect {

    public PerpetuallyGrantKeywordsToTargetCreatureEffect(Set<Keyword> keywords) {
        this(keywords, null, null);
    }

    public PerpetuallyGrantKeywordsToTargetCreatureEffect {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
        keywords = Set.copyOf(keywords);
        if ((triggeredAbilitySlot == null) != (triggeredAbility == null)) {
            throw new IllegalArgumentException("Triggered ability slot and effect must be provided together");
        }
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public PermanentPredicate filter() {
        return new PermanentControlledBySourceControllerPredicate();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter());
    }
}
