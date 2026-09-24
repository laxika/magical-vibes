package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "7")
public class ShellfishScholar extends Card {

    public ShellfishScholar() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.RAT),
                        new ConjureCardToGraveyardEffect("ISD", "83")));

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
