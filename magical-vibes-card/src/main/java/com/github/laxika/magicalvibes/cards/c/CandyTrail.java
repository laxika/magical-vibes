package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "243")
public class CandyTrail extends Card {

    public CandyTrail() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3), new DrawCardEffect()),
                "{2}, {T}, Sacrifice this artifact: You gain 3 life and draw a card."
        ));
    }
}
