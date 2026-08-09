package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "178")
public class DryadGreenseeker extends Card {

    public DryadGreenseeker() {
        // {T}: Look at the top card of your library. If it's a land card, you may reveal it and
        // put it into your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.LAND), false)),
                "{T}: Look at the top card of your library. If it's a land card, you may reveal it "
                        + "and put it into your hand."
        ));
    }
}
