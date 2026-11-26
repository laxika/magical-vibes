package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
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
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

@CardRegistration(set = "MSC", collectorNumber = "655")
public class DoombotHarbinger extends Card {

    public DoombotHarbinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new MillEffect(4, Subjects.you()),
                "Mill four cards?"));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(ExileGraveyardCardsEffect.cards(Subjects.sourceCard()),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                new QueueReflexiveAbilityEffect(ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .targetGraveyard(true)
                                        .build()))),
                "Exile Doombot Harbinger to return target creature card from your graveyard to your hand?"));
    }
}
