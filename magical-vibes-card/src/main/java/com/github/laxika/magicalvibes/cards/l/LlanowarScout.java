package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "170")
public class LlanowarScout extends Card {

    public LlanowarScout() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(CardType.LAND),
                        "Put a land card from your hand onto the battlefield?"
                )),
                "{T}: You may put a land card from your hand onto the battlefield."
        ));
    }
}
