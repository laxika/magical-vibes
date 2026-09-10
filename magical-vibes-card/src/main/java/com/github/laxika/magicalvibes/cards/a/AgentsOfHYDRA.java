package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "85")
public class AgentsOfHYDRA extends Card {

    public AgentsOfHYDRA() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Villain",
                2,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.VILLAIN),
                Set.of(Keyword.MENACE),
                Set.of()
        ));
    }
}
