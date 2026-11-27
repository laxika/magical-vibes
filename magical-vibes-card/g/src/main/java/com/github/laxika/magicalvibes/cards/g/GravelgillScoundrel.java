package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "53")
public class GravelgillScoundrel extends Card {

    public GravelgillScoundrel() {
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay(
                new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate(), true).withDescription("creature"),
                MakeCreatureUnblockableEffect.self())
                .withPrompt("Tap another untapped creature you control?"));
    }
}
