package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SHM", collectorNumber = "47")
public class PucasMischief extends Card {

    public PucasMischief() {
        // At the beginning of your upkeep, you may exchange control of target nonland permanent you
        // control and target nonland permanent an opponent controls with equal or lesser mana value.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExchangeControlOfTargetPermanentsEffect(),
                "You may exchange control of target nonland permanent you control and target nonland "
                        + "permanent an opponent controls with equal or lesser mana value."
        ));
    }
}
