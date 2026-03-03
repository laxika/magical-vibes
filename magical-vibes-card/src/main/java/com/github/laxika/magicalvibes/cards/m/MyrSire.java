package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "116")
public class MyrSire extends Card {

    public MyrSire() {
        // When this creature dies, create a 1/1 colorless Phyrexian Myr artifact creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateCreatureTokenEffect(
                1, "Phyrexian Myr", 1, 1, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR),
                Set.of(),
                Set.of(CardType.ARTIFACT)));
    }
}
