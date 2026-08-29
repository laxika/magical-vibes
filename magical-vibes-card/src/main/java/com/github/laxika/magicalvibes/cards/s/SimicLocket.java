package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "240")
public class SimicLocket extends Card {

    public SimicLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G/U}{G/U}{G/U}{G/U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{G/U}{G/U}{G/U}{G/U}, {T}, Sacrifice Simic Locket: Draw two cards."
        ));
    }
}
