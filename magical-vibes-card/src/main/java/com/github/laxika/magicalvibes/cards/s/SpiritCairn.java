package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "JUD", collectorNumber = "26")
public class SpiritCairn extends Card {

    public SpiritCairn() {
        MayPayManaEffect trigger = new MayPayManaEffect(
                "{W}",
                CreateTokenEffect.whiteSpirit(1),
                "Pay {W} to create a 1/1 white Spirit creature token with flying?"
        );
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, trigger);
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, trigger);
    }
}
