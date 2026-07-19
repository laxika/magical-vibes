package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "31")
public class MasterTransmuter extends Card {

    public MasterTransmuter() {
        // {U}, {T}, Return an artifact you control to its owner's hand:
        // You may put an artifact card from your hand onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsArtifactPredicate()),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.ARTIFACT), "artifact"),
                                "Put an artifact card from your hand onto the battlefield?"
                        )
                ),
                "{U}, {T}, Return an artifact you control to its owner's hand: "
                        + "You may put an artifact card from your hand onto the battlefield."
        ));
    }
}
