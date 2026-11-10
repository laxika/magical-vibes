package com.github.laxika.magicalvibes.model.condition;

/**
 * True when a creature other than the one the trigger event names is on the battlefield: Lifeline's intervening "if
 * another creature is on the battlefield" (CR 603.4). As a creature's death triggers the ability, the creatures that
 * died with it count, since a leaves-the-battlefield ability looks back in time (CR 603.10a), so the ability triggers
 * when every creature dies at once; as it resolves only the battlefield counts, so it then does nothing (Lifeline's
 * ruling).
 */
public record AnotherCreatureOnBattlefield() implements Condition {

    @Override
    public String conditionName() {
        return "another creature is on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "no other creature is on the battlefield";
    }
}
