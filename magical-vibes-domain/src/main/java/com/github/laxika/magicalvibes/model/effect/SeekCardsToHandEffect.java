package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly seeks up to the evaluated number of matching cards from the controller's library to hand. */
public record SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter, ManaValueBound manaValueBound)
        implements CombatDamageAmountAwareEffect {

    public SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter) {
        this(amount, filter, null);
    }

    public SeekCardsToHandEffect(CardPredicate filter) {
        this(new EventValue(), filter, null);
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue
                || manaValueBound != null && manaValueBound.amount() instanceof EventValue;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        if (amount instanceof EventValue) {
            return amount;
        }
        return manaValueBound == null ? null : manaValueBound.amount();
    }

    @Override
    public CardEffect snapshotCombatDamage(int damageDealt) {
        DynamicAmount snapshotAmount = amount instanceof EventValue ? new Fixed(damageDealt) : amount;
        ManaValueBound snapshotBound = manaValueBound == null || !(manaValueBound.amount() instanceof EventValue)
                ? manaValueBound
                : new ManaValueBound(new Fixed(damageDealt), manaValueBound.exact(), manaValueBound.offset());
        if (snapshotAmount == amount && snapshotBound == manaValueBound) {
            return this;
        }
        return new SeekCardsToHandEffect(snapshotAmount, filter, snapshotBound);
    }
}
