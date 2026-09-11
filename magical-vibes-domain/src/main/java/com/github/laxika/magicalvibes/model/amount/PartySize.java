package com.github.laxika.magicalvibes.model.amount;

/**
 * The maximum number of distinct party roles filled by creatures controlled by the controller.
 * A creature can fill only one of the Cleric, Rogue, Warrior, and Wizard roles.
 */
public record PartySize() implements DynamicAmount {
}
