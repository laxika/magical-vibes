package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "193")
public class HammerOfBogardan extends Card {

    public HammerOfBogardan() {
        // Hammer of Bogardan deals 3 damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

        // {2}{R}{R}{R}: Return this card from your graveyard to your hand. Activate only during your upkeep.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{R}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{R}{R}{R}: Return Hammer of Bogardan from your graveyard to your hand. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
