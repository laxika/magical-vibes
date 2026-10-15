package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "173")
public class FungalSprouting extends Card {

    public FungalSprouting() {
        // Create X 1/1 green Saproling creature tokens, where X is the greatest power
        // among creatures you control.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new GreatestPowerAmongControlled(), "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()
        ));
    }
}
