package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "98")
public class NightsquadCommando extends Card {

    public NightsquadCommando() {
        // When this creature enters, if you attacked this turn, create a 1/1 white Human Soldier creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Raid(),
                new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of())));
    }
}
