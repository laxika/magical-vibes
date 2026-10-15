package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "RNA", collectorNumber = "66")
public class CarrionImp extends Card {

    public CarrionImp() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        ExileGraveyardCardWithConditionalBonusEffect.creatureCardOnly(2),
                        "Exile target creature card from a graveyard?"));
    }
}
