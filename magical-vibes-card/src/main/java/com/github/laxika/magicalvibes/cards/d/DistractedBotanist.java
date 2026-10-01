package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YSOS", collectorNumber = "17")
public class DistractedBotanist extends Card {

    public DistractedBotanist() {
        addEffect(EffectSlot.ON_DEATH,
                new PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect(
                        SequenceEffect.of(new DrawCardEffect(1), new GainLifeEffect(1))));
    }
}
