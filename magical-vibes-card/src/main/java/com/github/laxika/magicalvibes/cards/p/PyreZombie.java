package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "261")
public class PyreZombie extends Card {

    public PyreZombie() {
        // At the beginning of your upkeep, if this card is in your graveyard, you may pay
        // {1}{B}{B}. If you do, return it to your hand.
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new MayPayManaEffect("{1}{B}{B}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Pay {1}{B}{B} to return Pyre Zombie from your graveyard to your hand?"));

        // {1}{R}{R}, Sacrifice this creature: It deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(2)),
                "{1}{R}{R}, Sacrifice this creature: It deals 2 damage to any target."
        ));
    }
}
