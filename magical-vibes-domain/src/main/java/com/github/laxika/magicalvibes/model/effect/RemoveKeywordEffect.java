package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot or continuous keyword removal for permanents selected by {@code subject}.
 *
 * <p>With {@link Subjects#target()} on a {@code Triggers.blocks()} / {@code Triggers.becomesBlocked()} ({@code EACH_BLOCKER})
 * ability whose card declares no target for it, the target is "that creature", the creature on the other side of the
 * block, which the collector records as the entry's non-targeting target (Talruum Champion).
 */
public record RemoveKeywordEffect(Keyword keyword, PermanentSubject subject, PermanentPredicate filter,
                                  Duration duration)
        implements CardEffect {

    public RemoveKeywordEffect(Keyword keyword, PermanentSubject subject) {
        this(keyword, subject, null, Duration.UNTIL_END_OF_TURN);
    }

    public RemoveKeywordEffect(Keyword keyword, PermanentSubject subject, PermanentPredicate filter) {
        this(keyword, subject, filter, Duration.UNTIL_END_OF_TURN);
    }

    public RemoveKeywordEffect(Keyword keyword, PermanentSubject subject, Duration duration) {
        this(keyword, subject, null, duration);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (subject instanceof PermanentSubject.Source) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.NONE;
    }
}
