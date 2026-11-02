package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "15")
@CardRegistration(set = "MAT", collectorNumber = "65")
@CardRegistration(set = "MAT", collectorNumber = "115")
@CardRegistration(set = "MAT", collectorNumber = "158")
@CardRegistration(set = "MAT", collectorNumber = "199")
public class UrborgScavengers extends Card {

    public UrborgScavengers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetCardFromGraveyardPutCounterOnSourceEffect());
        addTrigger(Triggers.attacks(),
                new ExileTargetCardFromGraveyardPutCounterOnSourceEffect());
        addStaticEffect(new GainKeywordsOfCardsExiledWithSourceEffect(false));
    }
}
