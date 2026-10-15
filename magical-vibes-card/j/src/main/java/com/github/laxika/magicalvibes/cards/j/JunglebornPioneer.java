package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "137")
public class JunglebornPioneer extends Card {

    public JunglebornPioneer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Merfolk",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.MERFOLK),
                Set.of(Keyword.HEXPROOF),
                Set.of()
        ));
    }
}
