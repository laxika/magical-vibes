package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "234")
public class DimirLocket extends Card {

    public DimirLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK))),
                "{T}: Add {U} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U/B}{U/B}{U/B}{U/B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{U/B}{U/B}{U/B}{U/B}, {T}, Sacrifice Dimir Locket: Draw two cards."
        ));
    }
}
