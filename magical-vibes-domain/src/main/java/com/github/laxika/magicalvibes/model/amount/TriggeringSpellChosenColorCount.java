package com.github.laxika.magicalvibes.model.amount;

/**
 * "For each of the chosen colors it is" (Tablet of the Guilds): the number of the triggering spell's effective colors
 * that are among the colors the ability's source chose, both read as the ability resolves (CR 608.2h): the spell while
 * it is on the stack, else as it last existed there; the source on the battlefield, else as it last existed there.
 */
public record TriggeringSpellChosenColorCount() implements DynamicAmount {
}
