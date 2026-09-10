package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

/**
 * Registers a delayed trigger for the source creature's controller's next upkeep. When that
 * trigger resolves, it reuses {@link GrantChosenKeywordEffect} to grant one basic-land-type
 * landwalk keyword until end of turn.
 */
public record GrantChosenLandwalkAtNextUpkeepEffect() implements CardEffect {

    public static final List<Keyword> LANDWALK_OPTIONS = List.of(
            Keyword.PLAINSWALK,
            Keyword.ISLANDWALK,
            Keyword.SWAMPWALK,
            Keyword.MOUNTAINWALK,
            Keyword.FORESTWALK);
}
