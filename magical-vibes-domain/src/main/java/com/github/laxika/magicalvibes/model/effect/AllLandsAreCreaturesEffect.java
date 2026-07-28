package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect that turns lands on the battlefield (all players') into a creature with the given
 * fixed power and toughness while still being a land. When {@code requiredSubtype} is {@code null}
 * every land is animated (Nature's Revolt = 2/2); otherwise only lands carrying that land subtype
 * are animated (Living Lands = all Forests become 1/1). A non-null {@code color} is the colour the
 * animated land becomes, replacing the colours it had (CR 105.3) — a land has no mana cost and so
 * is colourless on its own (CR 202.2), which is why Kormus Bell has to spell out "black" while
 * Living Lands and Nature's Revolt leave their animated lands colourless.
 *
 * <p>The layer-4 type change is applied by the layered pass; the colour (layer 5, CR 613.1e), base
 * P/T and creature-ness are filled in the accumulator pass by the matching handler, and
 * combat/targeting queries recognise animated lands via {@code GameQueryService.matchesAnimateLand}.
 */
public record AllLandsAreCreaturesEffect(int power, int toughness, CardSubtype requiredSubtype, CardColor color)
        implements CardEffect {

    public AllLandsAreCreaturesEffect(int power, int toughness) {
        this(power, toughness, null, null);
    }

    public AllLandsAreCreaturesEffect(int power, int toughness, CardSubtype requiredSubtype) {
        this(power, toughness, requiredSubtype, null);
    }
}
