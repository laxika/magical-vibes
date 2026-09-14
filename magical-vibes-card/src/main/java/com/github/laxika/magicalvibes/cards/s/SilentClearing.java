package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "HA7", collectorNumber = "24")
public class SilentClearing extends Card {

    public SilentClearing() {
        // {T}, Pay 1 life: Add {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.WHITE)),
                "{T}, Pay 1 life: Add {W}."
        ));

        // {T}, Pay 1 life: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.BLACK)),
                "{T}, Pay 1 life: Add {B}."
        ));

        // {1}, {T}, Sacrifice this land: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{1}, {T}, Sacrifice this land: Draw a card."
        ));
    }
}
