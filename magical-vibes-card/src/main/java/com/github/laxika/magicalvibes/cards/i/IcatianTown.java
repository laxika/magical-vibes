package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "25")
@CardRegistration(set = "5ED", collectorNumber = "38")
public class IcatianTown extends Card {

    public IcatianTown() {
        // Create four 1/1 white Citizen creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(4, "Citizen", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.CITIZEN), Set.of(), Set.of()));
    }
}
