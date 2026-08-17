package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "237")
public class GolgariLocket extends Card {

    public GolgariLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {B} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B/G}{B/G}{B/G}{B/G}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{B/G}{B/G}{B/G}{B/G}, {T}, Sacrifice Golgari Locket: Draw two cards."
        ));
    }
}
