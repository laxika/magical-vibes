package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Static effect: the permanent's controller — the player, not the permanent — has {@code keyword}.
 * Read by {@code GameQueryService.playerHasShroud} / {@code playerHasHexproof} while the source is
 * on that player's battlefield (True Believer, Ivory Mask; Leyline of Sanctity, Witchbane Orb,
 * Shalai, Spirit of the Hearth).
 * <p>
 * Only {@link Keyword#SHROUD} and {@link Keyword#HEXPROOF} are meaningful here: those are the two
 * "can't be the target of" qualities a player can have. A keyword that only a permanent can carry
 * would be silently inert, so it is rejected at construction.
 */
public record GrantControllerKeywordEffect(Keyword keyword) implements CardEffect {

    public GrantControllerKeywordEffect {
        if (keyword != Keyword.SHROUD && keyword != Keyword.HEXPROOF) {
            throw new IllegalArgumentException(
                    "GrantControllerKeywordEffect supports only SHROUD and HEXPROOF, got " + keyword);
        }
    }
}
