package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record BoostSelfPerOtherAttackingSubtypeEffect(
        CardSubtype subtype,
        int powerPerCreature,
        int toughnessPerCreature
) implements CardEffect {
}
