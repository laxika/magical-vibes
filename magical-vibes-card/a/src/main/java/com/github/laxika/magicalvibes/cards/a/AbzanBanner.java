package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "215")
public class AbzanBanner extends Card {

    public AbzanBanner() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {W}, {B}, or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{B}{G}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{W}{B}{G}, {T}, Sacrifice Abzan Banner: Draw a card."
        ));
    }
}
