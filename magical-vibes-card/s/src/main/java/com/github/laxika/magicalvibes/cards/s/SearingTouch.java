package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

/**
 * Buyback {4} — an optional additional cost that returns the spell to its owner's hand as it
 * resolves (CR 702.27) instead of putting it into the graveyard.
 */
@CardRegistration(set = "TMP", collectorNumber = "201")
@CardRegistration(set = "TPR", collectorNumber = "155")
public class SearingTouch extends Card {

    public SearingTouch() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{4}"));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
