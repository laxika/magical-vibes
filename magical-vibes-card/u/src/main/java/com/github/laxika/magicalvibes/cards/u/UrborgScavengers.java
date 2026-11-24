package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.attacks(),
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addStaticEffect(new GainKeywordsOfCreatureCardsExiledWithSourceEffect(Subjects.exiledWithSource(),
                GainKeywordsOfCreatureCardsExiledWithSourceEffect.COMMON_KEYWORDS, false));
    }
}
