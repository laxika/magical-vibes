package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Activated-ability cost: sacrifice every permanent the activating player controls that matches
 * {@code filter}. The payment is automatic because there is no choice about which matching
 * permanents are sacrificed; zero matching permanents is a legal payment.
 */
public record SacrificeAllMatchingPermanentsCost(PermanentPredicate filter) implements CostEffect {
}
