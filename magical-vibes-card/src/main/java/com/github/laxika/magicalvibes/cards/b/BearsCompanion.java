package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "167")
public class BearsCompanion extends Card {

    public BearsCompanion() {
        // When this creature enters, create a 4/4 green Bear creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Bear", 4, 4, CardColor.GREEN,
                        List.of(CardSubtype.BEAR), Set.of(), Set.of()));
    }
}
