package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Devour N (CR 702.82): "As this creature enters, you may sacrifice any number of creatures.
 * It enters with {@code multiplier} times that many +1/+1 counters on it." The multiplier may
 * itself be dynamic for cards such as Thromok the Insatiable.
 * <p>
 * An as-enters replacement effect (MTG rule 614): placed in {@code EffectSlot.ON_ENTER_BATTLEFIELD}
 * and handled during {@code BattlefieldEntryService.handleCreatureEnteredBattlefield} before ETB
 * triggers fire. The controller chooses which of their other creatures to sacrifice; the entering
 * creature receives {@code multiplier * sacrificedCount} +1/+1 counters and records the number of
 * creatures devoured on the permanent (read by {@code CreaturesDevoured}).
 */
public record DevourEffect(DynamicAmount multiplier) implements ReplacementEffect {

    public DevourEffect(int multiplier) {
        this(new Fixed(multiplier));
    }
}
