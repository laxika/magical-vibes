package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "98")
public class RotFarmSkeleton extends Card {

    public RotFarmSkeleton() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // {2}{B}{G}, Mill four cards: Return this card from your graveyard to the battlefield.
        // Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{G}",
                List.of(
                        new MillControllerCost(4),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{2}{B}{G}, Mill four cards: Return this card from your graveyard to the battlefield. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
