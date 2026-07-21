package com.github.laxika.magicalvibes.model.effect;

/**
 * Soulbond self-enter half (CR 702.94a): choose another unpaired creature you control and pair
 * this creature with it. Used under a {@link MayEffect} on {@code ON_ENTER_BATTLEFIELD}.
 */
public record SoulbondChoosePartnerEffect() implements CardEffect {
}
