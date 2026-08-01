package com.github.laxika.magicalvibes.model.effect;

/**
 * "Simultaneously, all phased-out creatures phase in and all creatures with phasing phase out."
 * (Time and Tide). Non-targeting one-shot: both halves are collected from the pre-resolution board,
 * then applied together via {@code PhasingService.applyTimeAndTide}, so a creature that phases in
 * does not immediately phase out again. Attachments follow their hosts (CR 702.26g). Only creatures
 * are selected for either half — phased-out noncreatures and noncreature permanents with phasing
 * are untouched.
 */
public record TimeAndTideEffect() implements CardEffect {
}
