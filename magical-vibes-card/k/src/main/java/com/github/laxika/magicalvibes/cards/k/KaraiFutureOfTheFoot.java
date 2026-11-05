package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "TMT", collectorNumber = "151")
public class KaraiFutureOfTheFoot extends Card {

    public KaraiFutureOfTheFoot() {
        addSneak("{2}{W}{B}");

        // Sneak is Karai's only alternative cost, and she can only have been cast for it this turn
        // while she is still the permanent that entered this turn.
        AllOf sneakCostPaidThisTurn = new AllOf(List.of(new CastForAlternateCost(), new CameUnderControlThisTurn()));
        // "Return target creature card … If her sneak cost was paid this turn, instead return that card to the
        // battlefield": which return happens can't change before the ability resolves, so each is an ability of its
        // own whose event condition picks it, and only one asks for its target (one ability with a shared graveyard
        // target is a later fix).
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(sneakCostPaidThisTurn)),
                returnCreatureToHand());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(sneakCostPaidThisTurn),
                returnCreatureToBattlefield());
    }

    private ReturnCardFromGraveyardEffect returnCreatureToHand() {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
    }

    private ReturnCardFromGraveyardEffect returnCreatureToBattlefield() {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
    }
}
