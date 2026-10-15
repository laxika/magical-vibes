package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaEqualToEnchantedPermanentManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "298")
public class CharmedPendant extends Card {

    public CharmedPendant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillControllerCost(1),
                        new AddManaEqualToEnchantedPermanentManaCostEffect(true)
                ),
                "{T}, Mill a card: For each colored mana symbol in the milled card's mana cost, add one mana of that color."
        ));
    }
}
