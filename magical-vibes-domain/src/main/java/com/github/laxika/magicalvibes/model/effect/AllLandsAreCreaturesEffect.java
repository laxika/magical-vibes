package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect that turns lands on the battlefield (all players') into a creature with the given
 * fixed power and toughness while still being a land. When {@code requiredSubtype} is {@code null}
 * every land is animated (Nature's Revolt = 2/2); otherwise only lands carrying that land subtype
 * are animated (Living Lands = all Forests become 1/1; Kormus Bell = all Swamps become 1/1, and the
 * loader already colors those lands by their identity, so they read as black). The layer-4 type
 * change is applied by the layered pass; the base P/T and creature-ness are filled in the
 * accumulator pass by the matching handler, and combat/targeting queries recognise animated lands
 * via {@code GameQueryService.matchesAnimateLand}.
 */
public record AllLandsAreCreaturesEffect(int power, int toughness, CardSubtype requiredSubtype) implements CardEffect {

    public AllLandsAreCreaturesEffect(int power, int toughness) {
        this(power, toughness, null);
    }
}
