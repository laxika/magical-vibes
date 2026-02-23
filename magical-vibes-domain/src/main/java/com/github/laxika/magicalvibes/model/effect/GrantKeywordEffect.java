package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

public record GrantKeywordEffect(Keyword keyword, Scope scope) implements CardEffect {
    @Override public boolean canTargetPermanent() { return scope == Scope.TARGET; }

    public enum Scope {
        SELF,
        TARGET,
        ENCHANTED_CREATURE,
        EQUIPPED_CREATURE,
        OWN_TAPPED_CREATURES,
        OWN_CREATURES
    }
}
