package com.github.laxika.magicalvibes.model.effect;

/** Makes the owner of the permanent that caused this trigger lose its mana value in life. */
public record LoseLifeToOwnerOfTriggeringPermanentEffect() implements TriggeringPermanentManaValueEffect {
}
