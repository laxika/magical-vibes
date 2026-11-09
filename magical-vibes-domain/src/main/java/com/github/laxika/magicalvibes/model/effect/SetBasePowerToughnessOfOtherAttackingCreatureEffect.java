package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the base power and toughness of the other creature the source attacked with until end of turn ("you may have
 * that creature's base power and toughness become 4/3", Creepy Puppeteer). "That creature" is read as the ability
 * resolves from the creatures declared as attackers with the source, which the attack collector records on the stack
 * entry ({@code StackEntry.getAttackingPermanentSnapshots()}); with no single other one, nothing happens.
 */
public record SetBasePowerToughnessOfOtherAttackingCreatureEffect(int power, int toughness) implements CardEffect {
}
