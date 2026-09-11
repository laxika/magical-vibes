package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VMA", collectorNumber = "199")
public class BrindleShoat extends Card {

    public BrindleShoat() {
        // When this creature dies, create a 3/3 green Boar creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Boar", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.BOAR), Set.of(), Set.of()));
    }
}
