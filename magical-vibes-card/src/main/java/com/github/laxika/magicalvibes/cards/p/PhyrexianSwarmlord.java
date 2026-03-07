package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerOpponentPoisonCounterEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "119")
public class PhyrexianSwarmlord extends Card {

    public PhyrexianSwarmlord() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenPerOpponentPoisonCounterEffect(
                "Phyrexian Insect",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.INSECT),
                Set.of(Keyword.INFECT),
                Set.of()
        ));
    }
}
