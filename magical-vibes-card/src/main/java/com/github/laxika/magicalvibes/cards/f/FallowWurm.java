package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "WTH", collectorNumber = "126")
public class FallowWurm extends Card {

    public FallowWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessDiscardCardTypeEffect(CardType.LAND));
    }
}
