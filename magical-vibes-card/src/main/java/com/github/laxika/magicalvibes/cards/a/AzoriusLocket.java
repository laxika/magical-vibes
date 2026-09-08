package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "231")
public class AzoriusLocket extends Card {

    public AzoriusLocket() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE))),
                "{T}: Add {W} or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W/U}{W/U}{W/U}{W/U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{W/U}{W/U}{W/U}{W/U}, {T}, Sacrifice Azorius Locket: Draw two cards."
        ));
    }
}
