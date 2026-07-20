package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "88")
public class DreadWanderer extends Card {

    public DreadWanderer() {
        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {2}{B}: Return this card from your graveyard to the battlefield. Activate only as a
        // sorcery and only if you have one or fewer cards in hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{B}: Return this card from your graveyard to the battlefield. Activate only as a "
                        + "sorcery and only if you have one or fewer cards in hand.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withMaxCardsInHand(1));
    }
}
