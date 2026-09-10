package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "238")
public class TeachersPest extends Card {

    public TeachersPest() {
        // Whenever this creature attacks, you gain 1 life.
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(1));

        // {B}{G}: Return this card from your graveyard to the battlefield tapped.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .build()),
                "{B}{G}: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
