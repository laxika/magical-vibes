package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * "You may pay {@code manaCost} (which contains an {X}). If you do, it deals X damage to any
 * target." Models Flameblast Dragon's attack trigger. The any-target is chosen when the ability
 * is put on the stack (via the Attack targeting pipeline, since {@link #targetSpec()} includes
 * ANY_TARGET); the decision whether to pay, and the value of X, are made during resolution — the
 * handler prompts for X (capped by {@link com.github.laxika.magicalvibes.model.ManaCost#calculateMaxX},
 * which reserves the colored part of {@code manaCost}), pays {@code manaCost}, then deals X damage.
 * Choosing X=0 declines. Do NOT use {@code MayPayManaEffect("{X}…", …)} — it can't pay/plumb {X}
 * at resolution.
 *
 * @param manaCost the payable cost including an {X} symbol, e.g. {@code "{X}{R}"}
 */
public record PayXManaDealXDamageToAnyTargetEffect(String manaCost) implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new XValue();
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
