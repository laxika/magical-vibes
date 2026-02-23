package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;

public record GrantEffect(CardEffect effect, Scope scope) implements CardEffect {
}
