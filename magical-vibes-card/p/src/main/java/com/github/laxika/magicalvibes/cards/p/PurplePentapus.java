package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "233")
public class PurplePentapus extends Card {

    public PurplePentapus() {
        // When this creature enters, surveil 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));

        // {2}{B}, Tap an untapped creature you control: Return this card from your graveyard to the
        // battlefield tapped.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()
                ),
                "{2}{B}, Tap an untapped creature you control: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
