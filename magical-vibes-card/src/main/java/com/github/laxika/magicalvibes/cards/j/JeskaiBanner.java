package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "222")
public class JeskaiBanner extends Card {

    public JeskaiBanner() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED, ManaColor.WHITE))),
                "{T}: Add {U}, {R}, or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{R}{W}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{U}{R}{W}, {T}, Sacrifice Jeskai Banner: Draw a card."
        ));
    }
}
