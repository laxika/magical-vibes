package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "231")
public class BorosLocket extends Card {

    public BorosLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))),
                "{T}: Add {R} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R/W}{R/W}{R/W}{R/W}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{R/W}{R/W}{R/W}{R/W}, {T}, Sacrifice Boros Locket: Draw two cards."
        ));
    }
}
