package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "233")
public class ThranTemporalGateway extends Card {

    public ThranTemporalGateway() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(new CardIsHistoricPredicate(), new CardIsPermanentPredicate())),
                                "historic permanent"
                        ),
                        "Put a historic permanent card from your hand onto the battlefield?"
                )),
                "{4}, {T}: You may put a historic permanent card from your hand onto the battlefield."
        ));
    }
}
