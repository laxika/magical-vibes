package com.github.laxika.magicalvibes.model.condition;

/** Whether the current attack directly attacks this Aura's enchanted player with a creature. */
public record AttacksEnchantedPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "attacks enchanted player with one or more creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return "did not attack enchanted player with a creature";
    }
}
