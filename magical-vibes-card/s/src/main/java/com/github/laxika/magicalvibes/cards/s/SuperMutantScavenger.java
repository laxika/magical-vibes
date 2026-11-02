package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "85")
@CardRegistration(set = "PIP", collectorNumber = "613")
public class SuperMutantScavenger extends Card {

    public SuperMutantScavenger() {
        CardAnyOfPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
        target(new GraveyardCardPredicateTargetFilter(
                auraOrEquipment, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);

        ReturnCardFromGraveyardEffect returnAuraOrEquipment = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(auraOrEquipment)
                .targetGraveyard(true)
                .upTo(true)
                .build();
        addTrigger(Triggers.enters(ObjectRelation.THIS), returnAuraOrEquipment);
        addTrigger(Triggers.dies(ObjectRelation.THIS), returnAuraOrEquipment);
    }
}
