package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

@CardRegistration(set = "BRO", collectorNumber = "165")
public class SkitterbeamBattalion extends Card {

    public SkitterbeamBattalion() {
        addPrototype("{3}{R}{R}", CardColor.RED, 2, 2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new WasCast(), new CreateTokenCopyOfSourceEffect(false, 2)));
    }
}
