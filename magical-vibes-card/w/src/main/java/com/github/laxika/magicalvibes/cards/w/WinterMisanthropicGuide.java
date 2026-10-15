package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect;

@CardRegistration(set = "DSK", collectorNumber = "240")
public class WinterMisanthropicGuide extends Card {

    public WinterMisanthropicGuide() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EachPlayerDrawsCardEffect(2));
        addEffect(EffectSlot.STATIC,
                new SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect());
    }
}
