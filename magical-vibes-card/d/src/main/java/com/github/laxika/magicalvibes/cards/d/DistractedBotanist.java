package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSOS", collectorNumber = "17")
public class DistractedBotanist extends Card {

    public DistractedBotanist() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect(
                        SequenceEffect.of(new DrawCardEffect(1), new GainLifeEffect(1))));
    }
}
