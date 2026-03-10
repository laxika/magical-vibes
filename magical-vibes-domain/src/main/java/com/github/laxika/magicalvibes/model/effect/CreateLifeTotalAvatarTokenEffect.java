package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Create a creature token whose power and toughness are each equal to the controller's
 * life total. The token has a characteristic-defining ability (PowerToughnessEqualToControllerLifeTotalEffect)
 * as a static effect, so its P/T updates dynamically.
 *
 * @param tokenName name of the token (e.g. "Avatar")
 * @param color     the token's color
 * @param subtypes  the token's creature subtypes
 */
public record CreateLifeTotalAvatarTokenEffect(
        String tokenName,
        CardColor color,
        List<CardSubtype> subtypes
) implements CardEffect {
}
