package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "7")
public class ShellfishScholar extends Card {

    public ShellfishScholar() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.RAT))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new ConjureCardToGraveyardEffect("ISD", "83"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        new CardTruePredicate(), 2, Set.of(Zone.GRAVEYARD))),
                "{T}: Spells you cast from your graveyard this turn cost {2} less to cast."
        ).withActivationCondition(
                new GraveyardCardThreshold(7, null),
                "Activate only if seven or more cards are in your graveyard."));
    }
}
