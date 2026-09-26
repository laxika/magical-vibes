package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2483")
public class ShadowheartDarkJusticiar extends Card {

    public ShadowheartDarkJusticiar() {
        // {1}{B}, {T}, Sacrifice another creature: Draw X cards, where X is that creature's power.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(false, true, false, true),
                        new DrawCardEffect(new XValue())
                ),
                "{1}{B}, {T}, Sacrifice another creature: Draw X cards, where X is that creature's power."
        ));
    }
}
