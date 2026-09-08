package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "145")
public class AjaniMentorOfHeroes extends Card {

    public AjaniMentorOfHeroes() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))),
                "+1: Distribute three +1/+1 counters among one, two, or three target creatures you control.",
                null, +1, null, null,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureYouControl(),
                        TargetFilters.creatureYouControl()), 1, 3
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(4,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.AURA),
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER))))),
                "+1: Look at the top four cards of your library. You may reveal an Aura, creature, or planeswalker card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new GainLifeEffect(100)),
                "\u22128: You gain 100 life."
        ));
    }
}
