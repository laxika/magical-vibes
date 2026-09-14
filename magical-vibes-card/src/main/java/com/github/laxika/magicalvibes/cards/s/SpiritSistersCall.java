package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithTargetCardPredicate;

@CardRegistration(set = "NEO", collectorNumber = "237")
public class SpiritSistersCall extends Card {

    public SpiritSistersCall() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentSharesCardTypeWithTargetCardPredicate(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsPermanentPredicate())
                                .targetGraveyard(true)
                                .exileIfLeavesBattlefield(true)
                                .build(),
                        "a permanent that shares a card type with the chosen card",
                        true,
                        false),
                "Sacrifice a permanent that shares a card type with the chosen card?"));
    }
}
