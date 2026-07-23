package com.github.laxika.magicalvibes.model.effect;

/**
 * Oath of Lim-Dûl: "Whenever you lose life, for each 1 life you lost, sacrifice a permanent other
 * than this enchantment unless you discard a card." Slot {@code ON_CONTROLLER_LOSES_LIFE}. The life
 * lost is snapshotted onto the trigger's {@code eventValue}; at resolution the controller repeats
 * that many times: discard a card, or sacrifice a permanent other than the source (lands allowed).
 * If neither is possible for an iteration, that iteration is skipped. Reuses {@code GameData.torment}
 * progress + Torment sacrifice/discard plumbing (without a "lose life" option).
 */
public record SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect() implements CardEffect {
}
