package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

@CardRegistration(set = "TMT", collectorNumber = "70")
public class ParameciaColoniex extends Card {

    public ParameciaColoniex() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(3, Subjects.you()));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(new ExileSourceCardFromGraveyardEffect(),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                new QueueReflexiveAbilityEffect(ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.TOP_OF_CONTROLLERS_LIBRARY)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .targetGraveyard(true)
                                        .build()))),
                "Exile Paramecia Coloniex?"));
    }
}
