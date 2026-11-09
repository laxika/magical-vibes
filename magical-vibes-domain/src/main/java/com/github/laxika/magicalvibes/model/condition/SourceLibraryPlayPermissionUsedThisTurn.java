package com.github.laxika.magicalvibes.model.condition;

/**
 * The source's "once each turn, you may play a land or cast a spell from the top of your library" permission has been
 * used this turn (The Fourth Doctor's "when you do").
 */
public record SourceLibraryPlayPermissionUsedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "top-of-library play permission used this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "the top-of-library play permission wasn't used this turn";
    }
}
