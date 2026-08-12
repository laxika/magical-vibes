package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "225")
public class SultaiBanner extends Card {

    public SultaiBanner() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {B}, {G}, or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{G}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{B}{G}{U}, {T}, Sacrifice Sultai Banner: Draw a card."
        ));
    }
}
