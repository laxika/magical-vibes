package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * "The next time a source of the given color of your choice would deal damage to you this turn,
 * prevent that damage." The source is chosen on resolution (a permanent of that color). The shield
 * prevents the entire next damage event from that source to the controller, then is consumed —
 * unlike {@link PreventAllDamageFromChosenSourceEffect}, which prevents all damage that turn.
 * The Circle of Protection cycle.
 *
 * @param color the color a chosen source must be
 */
public record PreventNextDamageFromChosenColoredSourceEffect(CardColor color) implements CardEffect {
}
