package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "52")
@CardRegistration(set = "CMM", collectorNumber = "74")
@CardRegistration(set = "CMM", collectorNumber = "480")
@CardRegistration(set = "CMM", collectorNumber = "672")
public class AzamiLadyOfScrolls extends Card {

    public AzamiLadyOfScrolls() {
        // Tap an untapped Wizard you control: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                        new DrawCardEffect()),
                "Tap an untapped Wizard you control: Draw a card."
        ));
    }
}
