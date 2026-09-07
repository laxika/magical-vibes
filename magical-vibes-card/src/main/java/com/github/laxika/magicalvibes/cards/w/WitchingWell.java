package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "74")
public class WitchingWell extends Card {

    public WitchingWell() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{3}{U}, Sacrifice this artifact: Draw two cards."
        ));
    }
}
