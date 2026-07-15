package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

public record DealDamageToTargetControllerIfTargetHasKeywordEffect(int damage, Keyword keyword) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
