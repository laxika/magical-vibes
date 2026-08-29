package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "56")
public class FrostAugur extends Card {

    public FrostAugur() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{S}",
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardSupertypePredicate(CardSupertype.SNOW), false)),
                "{S}, {T}: Look at the top card of your library. If it's a snow card, you may reveal it "
                        + "and put it into your hand."
        ));
    }
}
