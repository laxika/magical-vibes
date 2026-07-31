package com.github.laxika.magicalvibes.model.condition;

/**
 * True if the source permanent attacked and became blocked at any point this turn (read from the
 * turn-scoped {@code GameData.creaturesBlockedThisTurn} set recorded at declare-blockers time).
 * Blocking does not count — only the "was blocked" direction. Because the set is keyed by permanent
 * ID, the condition stays answerable after the source has left the battlefield, which is what a dies
 * trigger's intervening-"if" needs. Used by Fyndhorn Druid.
 */
public record SourceWasBlockedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source was blocked this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature was not blocked this turn";
    }
}
