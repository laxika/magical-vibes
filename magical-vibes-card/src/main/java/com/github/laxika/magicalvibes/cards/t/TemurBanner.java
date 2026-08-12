package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "226")
public class TemurBanner extends Card {

    public TemurBanner() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED))),
                "{T}: Add {G}, {U}, or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{U}{R}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{G}{U}{R}, {T}, Sacrifice Temur Banner: Draw a card."
        ));
    }
}
