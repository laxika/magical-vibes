package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "127")
public class AjaniUnyielding extends Card {

    public AjaniUnyielding() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new LookAtTopCardsEffect(
                        new Fixed(3), new Fixed(3), new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        LookDestination.BOTTOM_OF_LIBRARY, true)),
                "+2: Reveal the top three cards of your library. Put all nonland permanent cards revealed this way into your hand and the rest on the bottom of your library in any order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new GainLifeEffect(new TargetPower(), GainLifeRecipient.TARGET_CONTROLLER),
                        new ExileTargetPermanentEffect()
                ),
                "-2: Exile target creature. Its controller gains life equal to its power.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 5, new PermanentIsCreaturePredicate()),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.LOYALTY, 5,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsPlaneswalkerPredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                )))
                ),
                "-9: Put five +1/+1 counters on each creature you control and five loyalty counters on each other planeswalker you control."
        ));
    }
}
