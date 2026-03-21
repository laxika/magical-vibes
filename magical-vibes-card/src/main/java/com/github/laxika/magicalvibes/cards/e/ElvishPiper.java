package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "262")
@CardRegistration(set = "M10", collectorNumber = "177")
public class ElvishPiper extends Card {

    public ElvishPiper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                        "Put a creature card from your hand onto the battlefield?"
                )),
                "{G}, {T}: You may put a creature card from your hand onto the battlefield."
        ));
    }
}
