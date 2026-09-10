package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControllerAndTargetPlayerLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OHOP", collectorNumber = "5")
public class CliffsideMarket extends Card {

    public CliffsideMarket() {
        MayEffect exchangeLifeTotals = new MayEffect(
                new ExchangeControllerAndTargetPlayerLifeTotalsEffect(),
                "Exchange life totals with target player?");
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, exchangeLifeTotals);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, exchangeLifeTotals);

        setMultiTargetConstraint(MultiTargetConstraint.SHARE_CARD_TYPE);
        target(TargetFilters.permanent());
        target(TargetFilters.permanent()).addEffect(
                EffectSlot.CHAOS_TRIGGERED,
                ExchangeControlOfTargetPermanentsEffect.withSharedCardType(new PermanentTruePredicate()));
    }
}
