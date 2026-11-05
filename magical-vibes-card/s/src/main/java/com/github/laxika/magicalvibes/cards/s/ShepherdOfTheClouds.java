package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "28")
public class ShepherdOfTheClouds extends Card {

    public ShepherdOfTheClouds() {
        CardPredicate permanentWithManaValueThreeOrLess = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardMaxManaValuePredicate(3)));
        ControlsPermanent controlsMount = new ControlsPermanent(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNT));

        target(new GraveyardCardPredicateTargetFilter(
                permanentWithManaValueThreeOrLess, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                        new ConditionalEffect(
                                controlsMount,
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(permanentWithManaValueThreeOrLess)
                                        .targetGraveyard(true)
                                        .chooseAuraAttachment(true)
                                        .build()),
                        new ConditionalEffect(
                                new NotCondition(controlsMount),
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(permanentWithManaValueThreeOrLess)
                                        .targetGraveyard(true)
                                        .build())));
    }
}
