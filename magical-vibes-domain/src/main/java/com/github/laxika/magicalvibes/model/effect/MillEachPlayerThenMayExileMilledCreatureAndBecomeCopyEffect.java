package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Mills each player, then optionally exiles one creature card milled this way to copy it. */
public record MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect(List<Card> milledCreatureCards)
        implements CardEffect {

    public MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect() {
        this(List.of());
    }

    public MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect {
        milledCreatureCards = List.copyOf(milledCreatureCards);
    }
}
