package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Exile creature cards from graveyards and create a token for each card exiled this way.
 *
 * <p>Card-selection modes:
 * <ul>
 *   <li>{@code targetPlayerGraveyard = false} (default): exile the effect's {@code targetCardIds}
 *       (individual creature cards chosen at cast time) — Midnight Ritual's "exile X target creature
 *       cards from a single graveyard", Hour of Eternity's X-scaled multi-graveyard choice.</li>
 *   <li>{@code targetPlayerGraveyard = true}: exile ALL creature cards from the single targeted
 *       player's graveyard — Necromancer's Covenant's "exile all creature cards from target player's
 *       graveyard".</li>
 * </ul>
 *
 * <p>Token modes:
 * <ul>
 *   <li>Generic ({@code copyExiledCards = false}): a plain 2/2 black Zombie creature token per
 *       exiled card (Midnight Ritual, Necromancer's Covenant).</li>
 *   <li>Copy ({@code copyExiledCards = true}): a token that is a copy of the exiled card, with the
 *       Eternalize-style "except it's a 4/4 black Zombie" transformation applied (Hour of Eternity).
 *       The {@code colorOverride} replaces the copy's color, {@code addedSubtype} is added in
 *       addition to the copy's other creature types, and {@code powerOverride}/{@code toughnessOverride}
 *       set the copy's base P/T.</li>
 * </ul>
 *
 * @param targetPlayerGraveyard whether the effect exiles a target player's whole graveyard (creature
 *                              cards only) instead of individually targeted cards
 * @param copyExiledCards       when {@code true}, each token is a copy of the exiled card instead of a plain token
 * @param powerOverride         copy mode: base power of the token copy (e.g. 4)
 * @param toughnessOverride     copy mode: base toughness of the token copy (e.g. 4)
 * @param colorOverride         copy mode: the copy's color is set to exactly this color (e.g. black)
 * @param addedSubtype          copy mode: creature subtype added to the copy in addition to its other types (e.g. Zombie)
 */
public record ExileCreaturesFromGraveyardAndCreateTokensEffect(
        boolean targetPlayerGraveyard,
        boolean copyExiledCards,
        Integer powerOverride,
        Integer toughnessOverride,
        CardColor colorOverride,
        CardSubtype addedSubtype
) implements CardEffect {

    /** Midnight Ritual: create a plain 2/2 black Zombie creature token per exiled creature (not a copy). */
    public ExileCreaturesFromGraveyardAndCreateTokensEffect() {
        this(false, false, null, null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPlayerGraveyard ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
