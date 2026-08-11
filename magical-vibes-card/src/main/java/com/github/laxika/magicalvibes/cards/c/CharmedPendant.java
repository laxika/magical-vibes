package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.LastMilledCardColorSymbols;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
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
                        new AwardManaEffect(ManaColor.WHITE, new LastMilledCardColorSymbols(ManaColor.WHITE)),
                        new AwardManaEffect(ManaColor.BLUE, new LastMilledCardColorSymbols(ManaColor.BLUE)),
                        new AwardManaEffect(ManaColor.BLACK, new LastMilledCardColorSymbols(ManaColor.BLACK)),
                        new AwardManaEffect(ManaColor.RED, new LastMilledCardColorSymbols(ManaColor.RED)),
                        new AwardManaEffect(ManaColor.GREEN, new LastMilledCardColorSymbols(ManaColor.GREEN))
                ),
                "{T}, Mill a card: For each colored mana symbol in the milled card's mana cost, add one mana of that color."
        ));
    }
}
