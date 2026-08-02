package com.github.laxika.magicalvibes.model.effect;

/**
 * "At the beginning of your upkeep, you may remove a +1/+1 counter from this creature. If you don't,
 * sacrifice this creature and it deals damage equal to the number of +1/+1 counters on it to each
 * creature without flying and each player." (Magmasaur)
 *
 * <p>Not a {@link ForcedCostOrElseEffect}: its handler supports neither a counter-removal cost nor a
 * mass-damage fallback, and the damage amount has to be read from the source before it is sacrificed
 * (last known information). At resolution the controller is asked whether to remove a counter;
 * declining — or having no +1/+1 counter, in which case no prompt is shown — sacrifices the source
 * and deals that many damage to each creature without flying and each player.
 */
public record MagmasaurUpkeepEffect() implements CardEffect {
}
