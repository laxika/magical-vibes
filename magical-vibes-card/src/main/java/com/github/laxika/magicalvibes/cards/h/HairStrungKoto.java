package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "252")
public class HairStrungKoto extends Card {

    public HairStrungKoto() {
        // Tap an untapped creature you control: Target player mills a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "Tap an untapped creature you control: Target player mills a card."
        ));
    }
}
