package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "48")
public class EidolonOfPhilosophy extends Card {

    public EidolonOfPhilosophy() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(3)),
                "{6}{U}, Sacrifice Eidolon of Philosophy: Draw three cards."
        ));
    }
}
