package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "167")
@CardRegistration(set = "VOC", collectorNumber = "153")
public class DovinGrandArbiter extends Card {

    public DovinGrandArbiter() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RegisterDelayedCombatDamageEffect(
                        new PutCountersOnSourceCardEffect(CounterType.LOYALTY))),
                "+1: Until end of turn, whenever a creature you control deals combat damage to a player, put a loyalty counter on Dovin."));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(
                        new CreateTokenEffect(
                                "Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT)),
                        new GainLifeEffect(1)
                ),
                "\u22121: Create a 1/1 colorless Thopter artifact creature token with flying. You gain 1 life."));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(new Fixed(10), 3)),
                "\u22127: Look at the top ten cards of your library. Put three of them into your hand and the rest on the bottom of your library in a random order."));
    }
}
