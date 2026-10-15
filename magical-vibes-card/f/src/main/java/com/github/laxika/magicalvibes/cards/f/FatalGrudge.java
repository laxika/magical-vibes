package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;

@CardRegistration(set = "SNC", collectorNumber = "187")
public class FatalGrudge extends Card {

    public FatalGrudge() {
        addEffect(EffectSlot.SPELL, SacrificePermanentCost.withPermanentSnapshot(
                new PermanentNotPredicate(new PermanentIsLandPredicate()), "a nonland permanent"));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(1,
                new PermanentSharesCardTypeWithSourcePermanentPredicate(), SacrificeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
