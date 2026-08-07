package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this creature enters, sacrifice any number of creatures. This creature's power becomes the
 * total power of those creatures and its toughness becomes their total toughness."
 * <p>
 * An as-enters replacement effect (CR 614.1c): placed in {@code EffectSlot.ON_ENTER_BATTLEFIELD}
 * and handled during {@code BattlefieldEntryService.handleCreatureEnteredBattlefield} before ETB
 * triggers fire. The controller picks any number of their <em>other</em> creatures (zero is legal);
 * the totals are read from the sacrificed creatures' last known power/toughness on the battlefield
 * and stamped onto the entering permanent as a durable base-P/T override (layer 7b), so the value
 * is fixed once and later counters or boosts apply on top of it. Sacrificing nothing leaves it a
 * 0/0 that dies to state-based actions. Dracoplasm.
 */
public record SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect() implements ReplacementEffect {
}
