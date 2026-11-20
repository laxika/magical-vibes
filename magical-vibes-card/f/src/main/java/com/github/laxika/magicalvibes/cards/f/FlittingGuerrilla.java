package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;


@CardRegistration(set = "MOM", collectorNumber = "105")
public class FlittingGuerrilla extends Card {

    public FlittingGuerrilla() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new MillEffect(2, Subjects.eachPlayer()),
                new MayEffect(
                        SequenceEffect.of(new ExileSourceCardFromGraveyardEffect(),
                                new ConditionalEffect(HappenedThisWay.exiled(), new QueueReflexiveAbilityEffect(
                                        ReturnCardFromGraveyardEffect.builder()
                                                .destination(GraveyardChoiceDestination.TOP_OF_CONTROLLERS_LIBRARY)
                                                .filter(new CardAnyOfPredicate(List.of(
                                                        new CardTypePredicate(CardType.CREATURE),
                                                        new CardTypePredicate(CardType.BATTLE))))
                                                .targetGraveyard(true)
                                                .build()))),
                        "Exile Flitting Guerrilla?")));
    }
}
