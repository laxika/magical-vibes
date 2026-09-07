package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "228")
public class SelesnyaEvangel extends Card {

    public SelesnyaEvangel() {
        // {1}, {T}, Tap an untapped creature you control: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate(), true),
                        new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                ),
                "{1}, {T}, Tap an untapped creature you control: Create a 1/1 green Saproling creature token."
        ));
    }
}
