package com.github.laxika.magicalvibes.model.amount;

/**
 * The current life total of the controller of the spell/ability/permanent the amount
 * belongs to (e.g. Serra Avatar / Ajani Goldmane's Avatar token, whose power and
 * toughness are each equal to their controller's life total).
 */
public record ControllerLifeTotal() implements DynamicAmount {
}
