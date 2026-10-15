package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.NonSubtypeCreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "88")
public class CultConscript extends Card {

    public CultConscript() {
        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {1}{B}: Return this card from your graveyard to the battlefield. Activate only if a
        // non-Skeleton creature died under your control this turn.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{1}{B}: Return this card from your graveyard to the battlefield. Activate only if a "
                        + "non-Skeleton creature died under your control this turn."
        ).withActivationCondition(
                new NonSubtypeCreatureDiedUnderYourControlThisTurn(CardSubtype.SKELETON),
                "Activate only if a non-Skeleton creature died under your control this turn."
        ));
    }
}
