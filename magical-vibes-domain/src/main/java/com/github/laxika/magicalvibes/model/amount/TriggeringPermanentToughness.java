package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective toughness of the permanent that caused a trigger, evaluated when the trigger
 * resolves. If that permanent has left the battlefield, its last-known toughness is used.
 */
public record TriggeringPermanentToughness() implements DynamicAmount {
}
