package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Buyback {3} — an optional additional cost that returns the spell to its owner's hand as it
 * resolves (CR 702.27).
 * <p>
 * The creature is not forced to attack any particular player or planeswalker, so
 * {@code MustAttackThisTurnEffect(false)}.
 */
@CardRegistration(set = "TMP", collectorNumber = "138")
public class ImpsTaunt extends Card {

    public ImpsTaunt() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{3}"));
        addEffect(EffectSlot.SPELL, new MustAttackThisTurnEffect(false));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
        target(TargetFilters.creature());
    }
}
