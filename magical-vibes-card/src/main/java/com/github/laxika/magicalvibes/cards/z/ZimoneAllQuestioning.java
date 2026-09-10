package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsPrimeNumberOfLands;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "241")
public class ZimoneAllQuestioning extends Card {

    public ZimoneAllQuestioning() {
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        AllConditions triggerCondition = new AllConditions(List.of(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 1),
                new ControllerControlsPrimeNumberOfLands()));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                triggerCondition,
                SequenceEffect.of(
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Primo, the Indivisible", 0, 0,
                                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                                List.of(CardSubtype.FRACTAL), Set.of(), Set.of(),
                                false, false, Map.of(), List.of(), false, false, true, 0, Set.of()),
                        new PutCountersOnCreatedPermanentsEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, landsYouControl))));
    }
}
