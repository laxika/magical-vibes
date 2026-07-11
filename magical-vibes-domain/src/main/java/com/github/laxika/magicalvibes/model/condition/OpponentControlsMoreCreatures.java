package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent controls at least {@code minimumCreatureDifference} more creatures than the
 * controller (e.g. Avatar of Might: "if an opponent controls at least four more creatures than you").
 */
public record OpponentControlsMoreCreatures(int minimumCreatureDifference) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent controls more creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls enough more creatures";
    }
}
