package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelectedPermanentFollowUp;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "25")
public class NickFuryAgentOfSHIELD extends Card {

    public NickFuryAgentOfSHIELD() {
        CardAnyOfPredicate heroEquipmentOrVehicle = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.HERO),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                                7, heroEquipmentOrVehicle, new TransformSelectedPermanentFollowUp())
                ),
                "Power-up — {W}{U}{B}{R}{G}: Put two +1/+1 counters on Nick Fury, then look at the top "
                        + "seven cards of your library. You may put a Hero, Equipment, or Vehicle card "
                        + "from among them onto the battlefield. If it's a double-faced card, you may "
                        + "transform it. Put the rest on the bottom of your library in a random order. "
                        + "Activate each power-up ability only once. Reduce the cost by his mana cost if "
                        + "he entered this turn."
        ).withPowerUp());
    }
}
