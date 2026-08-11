package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "143")
public class FirebrandRanger extends Card {

    public FirebrandRanger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(CardPredicateUtils.basicLand(), "basic land"),
                        "Put a basic land card from your hand onto the battlefield?"
                )),
                "{G}, {T}: You may put a basic land card from your hand onto the battlefield."
        ));
    }
}
