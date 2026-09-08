package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "64")
public class InfestationSage extends Card {

    public InfestationSage() {
        // When this creature dies, create a 1/1 black and green Insect creature token with flying.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Insect", 1, 1, CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING), Set.of()));
    }
}
