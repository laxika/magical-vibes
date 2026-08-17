package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "118")
public class BogWitch extends Card {

    public BogWitch() {
        // {B}, {T}, Discard a card: Add {B}{B}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new DiscardCardTypeCost(null, null), new AwardManaEffect(ManaColor.BLACK, 3)),
                "{B}, {T}, Discard a card: Add {B}{B}{B}."
        ));
    }
}
