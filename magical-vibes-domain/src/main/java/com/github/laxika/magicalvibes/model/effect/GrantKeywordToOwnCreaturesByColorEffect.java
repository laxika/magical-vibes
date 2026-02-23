package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;

public record GrantKeywordToOwnCreaturesByColorEffect(CardColor color, Keyword keyword) implements CardEffect {
}
