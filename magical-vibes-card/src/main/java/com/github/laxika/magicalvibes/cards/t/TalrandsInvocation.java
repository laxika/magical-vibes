package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "73")
public class TalrandsInvocation extends Card {

    public TalrandsInvocation() {
        // Create two 2/2 blue Drake creature tokens with flying.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Drake", 2, 2,
                CardColor.BLUE,
                List.of(CardSubtype.DRAKE),
                Set.of(Keyword.FLYING),
                Set.<CardType>of()));
    }
}
