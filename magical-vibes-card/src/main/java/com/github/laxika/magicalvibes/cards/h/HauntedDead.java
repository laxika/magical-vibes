package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "116")
public class HauntedDead extends Card {

    public HauntedDead() {
        // When this creature enters, create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));

        // {1}{B}, Discard two cards: Return this card from your graveyard to the battlefield tapped.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()
                ),
                "{1}{B}, Discard two cards: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
