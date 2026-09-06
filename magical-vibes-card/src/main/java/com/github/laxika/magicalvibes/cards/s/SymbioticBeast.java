package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "287")
public class SymbioticBeast extends Card {

    public SymbioticBeast() {
        // When this creature dies, create four 1/1 green Insect creature tokens.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                4, "Insect", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.INSECT), Set.of(), Set.of()));
    }
}
