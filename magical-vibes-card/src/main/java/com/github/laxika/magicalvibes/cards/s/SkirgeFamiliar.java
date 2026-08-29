package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "157")
public class SkirgeFamiliar extends Card {

    public SkirgeFamiliar() {
        // Discard a card: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new AwardManaEffect(ManaColor.BLACK)),
                "Discard a card: Add {B}."
        ));
    }
}
