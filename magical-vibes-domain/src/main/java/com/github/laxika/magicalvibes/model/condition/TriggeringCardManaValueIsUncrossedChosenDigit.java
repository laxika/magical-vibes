package com.github.laxika.magicalvibes.model.condition;

/**
 * The mana value of what triggered the ability is a digit of the source's chosen number that isn't crossed out
 * (Duelists' Convocation International: "if it shares a mana value with one or more uncrossed digits in the chosen
 * number"). It reads the triggering spell as it is on the stack, X the number chosen for it, else the triggering card
 * (the land played).
 */
public record TriggeringCardManaValueIsUncrossedChosenDigit() implements Condition {

    @Override
    public String conditionName() {
        return "mana value shares an uncrossed digit";
    }

    @Override
    public String conditionNotMetReason() {
        return "no uncrossed digit shares its mana value";
    }
}
