package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "247")
@CardRegistration(set = "HA7", collectorNumber = "22")
@CardRegistration(set = "TLE", collectorNumber = "58")
@CardRegistration(set = "ACR", collectorNumber = "111")
public class SunbakedCanyon extends Card {

    public SunbakedCanyon() {
        // {T}, Pay 1 life: Add {R} or {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))
                ),
                "{T}, Pay 1 life: Add {R} or {W}."
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
