package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller chooses a card name, then reveals a card at random from their own hand. If the
 * revealed card has the chosen name, the source deals {@code damage} damage to any target chosen
 * when the ability was put on the stack. Used by Cursed Scroll.
 *
 * <p>The name is chosen while the ability resolves, so resolution pauses for a list choice and the
 * random reveal plus the conditional damage happen once the choice comes back. An empty hand means
 * nothing is revealed and no damage is dealt.
 *
 * @param damage how much damage the source deals when the revealed card matches the chosen name
 */
public record ChooseNameRevealRandomCardFromHandDealDamageEffect(int damage) implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
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
