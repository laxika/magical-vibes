package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds the source permanent's last noted type and amount of mana (see
 * {@link NoteManaSpentForActivationEffect}), spendable only to cast the last card exiled with that
 * permanent (Ice Cauldron). The exiled card is the one tracked as the source's imprint, and the mana
 * goes into the exiled-card-only bucket of {@link com.github.laxika.magicalvibes.model.ManaPool}.
 */
public record AddNotedManaForLastExiledCardEffect() implements CardEffect, ManaProducingEffect {
}
