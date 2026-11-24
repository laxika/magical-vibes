package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.StartingLifeTotal;

import java.util.Objects;

/**
 * Static replacement: "If you would lose the game, instead exile this permanent and your life total becomes N"
 * (The Golden Throne: 1; Exquisite Archangel: {@link StartingLifeTotal}). The amount is evaluated as the replacement
 * applies. With several loss replacements the losing player chooses the one that applies (CR 616.1).
 *
 * @param lifeTotal the life total the player's life becomes after the permanent is exiled
 */
public record ReplaceControllerLossWithExileAndLifeTotalEffect(DynamicAmount lifeTotal) implements CardEffect {

    public ReplaceControllerLossWithExileAndLifeTotalEffect {
        Objects.requireNonNull(lifeTotal, "lifeTotal");
    }

    public ReplaceControllerLossWithExileAndLifeTotalEffect(int lifeTotal) {
        this(new Fixed(lifeTotal));
    }

    /** "your life total becomes equal to your starting life total". */
    public static ReplaceControllerLossWithExileAndLifeTotalEffect startingLifeTotal() {
        return new ReplaceControllerLossWithExileAndLifeTotalEffect(new StartingLifeTotal());
    }
}
