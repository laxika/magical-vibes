package com.github.laxika.magicalvibes.model.amount;

/**
 * The highest life total among the opponents of the controller of the spell/ability/permanent the
 * amount belongs to (e.g. Malignus, whose power and toughness are each equal to half that value).
 * Evaluates to 0 when there is no opponent left.
 */
public record HighestOpponentLifeTotal() implements DynamicAmount {
}
