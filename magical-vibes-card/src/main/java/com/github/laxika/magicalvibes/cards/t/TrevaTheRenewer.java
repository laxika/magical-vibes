package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerPermanentOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "INV", collectorNumber = "280")
public class TrevaTheRenewer extends Card {

    public TrevaTheRenewer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}{W}", new GainLifePerPermanentOfChosenColorEffect(), "Pay {2}{W}?"));
    }
}
