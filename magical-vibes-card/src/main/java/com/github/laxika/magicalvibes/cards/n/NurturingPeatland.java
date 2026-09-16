package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "243")
@CardRegistration(set = "HA7", collectorNumber = "23")
public class NurturingPeatland extends Card {

    public NurturingPeatland() {
        // {T}, Pay 1 life: Add {B} or {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))
                ),
                "{T}, Pay 1 life: Add {B} or {G}."
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
