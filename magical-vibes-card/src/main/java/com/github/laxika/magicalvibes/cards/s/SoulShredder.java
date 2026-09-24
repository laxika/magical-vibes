package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.OneOrMoreCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "30")
public class SoulShredder extends Card {

    public SoulShredder() {
        OneOrMoreCreatureDeathTriggerEffect deathTrigger = new OneOrMoreCreatureDeathTriggerEffect(
                new PerpetuallyBoostSourceEffect(1, 1));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, deathTrigger);
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES, deathTrigger);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{1}, Sacrifice two creatures: Return Soul Shredder from your graveyard to the battlefield. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
