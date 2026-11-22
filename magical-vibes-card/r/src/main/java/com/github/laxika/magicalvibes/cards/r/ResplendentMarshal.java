package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "22")
@CardRegistration(set = "KHM", collectorNumber = "337")
public class ResplendentMarshal extends Card {

    public ResplendentMarshal() {
        MayEffect ability = new MayEffect(
                SequenceEffect.of(
                        ExileUpToOneCardFromGraveyardEffect.oneMatching(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardNotPredicate(new CardIsSelfPredicate()))))
                                .from(Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.exiled(), new QueueReflexiveAbilityEffect(
                                new PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1)))),
                "Exile another creature card from your graveyard?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), ability);
        addTrigger(Triggers.dies(ObjectRelation.THIS), ability);
    }
}
