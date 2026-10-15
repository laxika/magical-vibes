package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "98")
public class SaprazzanBreaker extends Card {

    public SaprazzanBreaker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new MillControllerThenIfMilledEffect(
                        1,
                        new CardTypePredicate(CardType.LAND),
                        new MakeCreatureUnblockableEffect(true))),
                "{U}: Mill a card. If a land card was milled this way, this creature can't be blocked this turn."
        ));
    }
}
