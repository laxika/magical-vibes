package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Adds mana of the given color. The quantity is a {@link DynamicAmount}, so the same
 * record covers a flat amount ("Add {G}"), a per-permanent count ("Add {G} for each Elf
 * you control" via {@code PermanentCount}), a counter count ("Add {C} for each charge
 * counter" via {@code CountersOnSource}), and a power-based amount ("Add {G} equal to
 * this creature's power" via {@code SourcePower}). Implements {@link ManaProducingEffect}
 * so the engine treats the ability as a mana ability (CR 605.1a).
 */
public record AwardManaEffect(ManaColor color, DynamicAmount amount,
                              boolean tracksProducingSourceForSpellCastTriggers)
        implements ManaProducingEffect {

    public AwardManaEffect(ManaColor color, DynamicAmount amount) {
        this(color, amount, false);
    }

    public AwardManaEffect(ManaColor color) {
        this(color, new Fixed(1));
    }

    public AwardManaEffect(ManaColor color, int amount) {
        this(color, new Fixed(amount));
    }

    /** Marks this mana as carrying a source-specific spell-cast trigger rider. */
    public AwardManaEffect withProducingSourceForSpellCastTriggers() {
        return new AwardManaEffect(color, amount, true);
    }

    @Override
    public ManaColor estimatedManaColor() {
        return color;
    }

    @Override
    public DynamicAmount estimatedManaAmount() {
        return amount;
    }
}
