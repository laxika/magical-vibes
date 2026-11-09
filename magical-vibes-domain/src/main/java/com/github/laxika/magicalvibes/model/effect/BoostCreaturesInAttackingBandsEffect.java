package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each creature in that band gets +1/+1 until end of turn for each creature in that band" (Chatzuk, Mighty
 * Guitarist): on a "whenever two or more creatures you control attack in a band" ability ({@code EACH_BAND}), whose
 * attacking creatures are the band; N is the number of them still attacking in a band as it resolves.
 */
public record BoostCreaturesInAttackingBandsEffect() implements CardEffect {
}
