package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "86")
public class BatWhisperer extends Card {

    public BatWhisperer() {
        // When this creature enters, if an opponent lost life this turn, create a 1/1 black Bat creature token with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new CreateTokenEffect("Bat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of())));
    }
}
