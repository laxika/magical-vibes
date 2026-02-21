package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "262")
public class ElvishPiper extends Card {

    public ElvishPiper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(CardType.CREATURE),
                        "Put a creature card from your hand onto the battlefield?"
                )),
                false,
                "{G}, {T}: You may put a creature card from your hand onto the battlefield."
        ));
    }
}
