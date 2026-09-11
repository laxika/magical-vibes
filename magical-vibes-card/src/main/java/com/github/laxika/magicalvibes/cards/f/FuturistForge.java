package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "55")
public class FuturistForge extends Card {

    public FuturistForge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{3}{U}, Sacrifice this artifact: Draw two cards."
        ));
    }
}
