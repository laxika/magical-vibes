package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "186")
public class HauntOfTheDeadMarshes extends Card {

    public HauntOfTheDeadMarshes() {
        // When this creature enters, scry 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        // {2}{B}: Return this card from your graveyard to the battlefield tapped. Activate only if you control a legendary creature.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .build()),
                "{2}{B}: Return this card from your graveyard to the battlefield tapped. Activate only if you control a legendary creature."
        ).withRequiredControlledPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                )),
                1,
                "legendary creatures"));
    }
}
