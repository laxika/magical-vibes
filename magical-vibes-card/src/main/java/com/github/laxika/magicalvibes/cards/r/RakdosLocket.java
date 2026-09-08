package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "237")
public class RakdosLocket extends Card {

    public RakdosLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B/R}{B/R}{B/R}{B/R}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{B/R}{B/R}{B/R}{B/R}, {T}, Sacrifice Rakdos Locket: Draw two cards."
        ));
    }
}
