package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "YBRO", collectorNumber = "11")
public class TomakulPhoenix extends Card {

    public TomakulPhoenix() {
        addEffect(EffectSlot.ON_DEATH, new PerpetuallyBoostSourceEffect(2, 2));
        addEffect(EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
                MayPayManaEffect.dynamic(
                        "{X}{R}",
                        new SourceCardPower(),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Pay {X}{R}, where X is Tomakul Phoenix's power, to return it from your graveyard to the battlefield?"));
    }
}
