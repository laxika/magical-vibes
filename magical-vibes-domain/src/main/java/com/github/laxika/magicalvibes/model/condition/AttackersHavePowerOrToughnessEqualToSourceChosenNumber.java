package com.github.laxika.magicalvibes.model.condition;

/**
 * Whether any of the creatures attacking an attack trigger's attacked target has power or toughness equal to the source's
 * chosen number: "whenever one or more creatures attack one of your opponents, if any of those creatures have power or
 * toughness equal to the chosen number" (Squall, Gunblade Duelist). An intervening "if" (CR 603.4) of
 * {@code Triggers.playerAttacksOneOfYourOpponents()}, judged against the opponent attacked as the ability triggers and
 * against the stack entry's attacked target as it resolves.
 */
public record AttackersHavePowerOrToughnessEqualToSourceChosenNumber() implements Condition {

    @Override
    public String conditionName() {
        return "a creature attacking that player has power or toughness equal to the chosen number";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature attacking that player has power or toughness equal to the chosen number";
    }
}
