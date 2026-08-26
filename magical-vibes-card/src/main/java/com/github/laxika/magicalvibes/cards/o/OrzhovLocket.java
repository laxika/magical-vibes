package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "236")
public class OrzhovLocket extends Card {

    public OrzhovLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W/B}{W/B}{W/B}{W/B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{W/B}{W/B}{W/B}{W/B}, {T}, Sacrifice Orzhov Locket: Draw two cards."
        ));
    }
}
