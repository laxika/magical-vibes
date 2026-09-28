package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/** One-shot effect: every player gains the given keyword until end of turn. */
public record PlayersGainKeywordUntilEndOfTurnEffect(Keyword keyword) implements CardEffect {
}
