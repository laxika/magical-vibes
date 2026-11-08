package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the permanent the resolving trigger's event was about ("that creature's mana value", Arni
 * Metalbrow; "its mana value", Brainstealer Dragon; "with equal or lesser mana value", Kodama of the East Tree): the
 * stack entry's triggering permanent as it is now, else as it last existed on the battlefield (CR 608.2h). 0 when the
 * entry has no triggering permanent.
 */
public record TriggeringPermanentManaValue() implements DynamicAmount {
}
