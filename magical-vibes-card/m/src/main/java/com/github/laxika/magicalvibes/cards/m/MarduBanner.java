package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "224")
public class MarduBanner extends Card {

    public MarduBanner() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {R}, {W}, or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{W}{B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{R}{W}{B}, {T}, Sacrifice Mardu Banner: Draw a card."
        ));
    }
}
