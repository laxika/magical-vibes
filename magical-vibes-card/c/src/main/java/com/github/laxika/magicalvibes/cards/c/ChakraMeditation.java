package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "91")
@CardRegistration(set = "TLE", collectorNumber = "179")
public class ChakraMeditation extends Card {

    public ChakraMeditation() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        target(new GraveyardCardPredicateTargetFilter(
                instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(instantOrSorcery)
                        .targetGraveyard(true)
                        .upTo(true)
                        .build());

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                new DrawCardEffect(),
                ConditionalEffect.unless(
                        new NotCondition(new GraveyardCardThreshold(
                                3, new CardSubtypePredicate(CardSubtype.LESSON))),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
