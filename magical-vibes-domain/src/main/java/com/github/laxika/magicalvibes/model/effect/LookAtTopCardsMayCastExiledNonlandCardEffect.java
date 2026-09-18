package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top cards of the controller's library, then may exile a nonland card among them
 * and cast that card without paying its mana cost. The other cards are put on the bottom in a
 * random order.
 */
public record LookAtTopCardsMayCastExiledNonlandCardEffect(DynamicAmount count,
                                                           CardPredicate castPredicate)
        implements CombatDamageAmountAwareEffect {

    public LookAtTopCardsMayCastExiledNonlandCardEffect(int count) {
        this(new Fixed(count), null);
    }

    public LookAtTopCardsMayCastExiledNonlandCardEffect(DynamicAmount count) {
        this(count, null);
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return count;
    }
}
