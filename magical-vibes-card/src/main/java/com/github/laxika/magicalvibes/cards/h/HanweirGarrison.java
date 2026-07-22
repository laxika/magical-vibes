package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "157")
public class HanweirGarrison extends Card {

    public HanweirGarrison() {
        // Whenever this creature attacks, create two 1/1 red Human creature tokens
        // that are tapped and attacking.
        // (Melds with Hanweir Battlements.) — meld ability is on Battlements.
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                2, "Human", 1, 1, CardColor.RED, List.of(CardSubtype.HUMAN), true
        ));
    }
}
