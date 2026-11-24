package com.github.laxika.magicalvibes.model.filter;

/**
 * "Tap any number of untapped creatures you control": an untapped permanent that can become tapped now, so one under a
 * "can't become tapped unless it's attacking" restriction (Ood Sphere's chaos ability) is not offered unless it is
 * attacking. The candidates of a choice whose chosen permanents are then tapped ({@code TapPermanentsEffect} over
 * {@code thisWay(CHOOSE)}).
 */
public record PermanentCanBecomeTappedPredicate() implements PermanentPredicate {
}
