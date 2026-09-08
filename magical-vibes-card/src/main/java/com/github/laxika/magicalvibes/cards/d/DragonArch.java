package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "135")
public class DragonArch extends Card {

    public DragonArch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardIsMulticoloredPredicate())),
                                "multicolored creature"),
                        "Put a multicolored creature card from your hand onto the battlefield?"
                )),
                "{2}, {T}: You may put a multicolored creature card from your hand onto the battlefield."
        ));
    }
}
