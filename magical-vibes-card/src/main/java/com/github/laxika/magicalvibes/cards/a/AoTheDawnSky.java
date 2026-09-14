package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "2")
public class AoTheDawnSky extends Card {

    private static final CardAllOfPredicate NONLAND_PERMANENT_WITH_MANA_VALUE_AT_MOST_FOUR =
            new CardAllOfPredicate(List.of(
                    new CardIsPermanentPredicate(),
                    new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                    new CardMaxManaValuePredicate(4)));

    private static final PermanentPredicate CREATURE_OR_VEHICLE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));

    public AoTheDawnSky() {
        addEffect(EffectSlot.ON_DEATH, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top seven cards of your library. Put any number of nonland permanent cards with total mana value 4 or less from among them onto the battlefield. Put the rest on the bottom of your library in a random order.",
                        LookAtTopCardsEffect.mayPutUpToMatchingOntoBattlefieldRestOnBottomRandomWithinTotalManaValue(
                                7, NONLAND_PERMANENT_WITH_MANA_VALUE_AT_MOST_FOUR, 7, 4)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on each permanent you control that's a creature or Vehicle.",
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 2, CREATURE_OR_VEHICLE))
        )));
    }
}
