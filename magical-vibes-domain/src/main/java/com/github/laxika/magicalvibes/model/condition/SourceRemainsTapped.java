package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent is on the battlefield and has not untapped since it had untapped {@code untapSequence} times:
 * "for as long as this creature remains tapped". An untap ends it for good, even if the source is tapped again.
 *
 * @param untapSequence the source's untap count ({@code Permanent.getUntapSequence()}) as the duration began
 */
public record SourceRemainsTapped(int untapSequence) implements Condition {

    @Override
    public String conditionName() {
        return "source remains tapped";
    }

    @Override
    public String conditionNotMetReason() {
        return "source has untapped or left the battlefield";
    }
}
