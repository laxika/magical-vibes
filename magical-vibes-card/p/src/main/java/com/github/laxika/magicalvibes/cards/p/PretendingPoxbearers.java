package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "237")
public class PretendingPoxbearers extends Card {

    public PretendingPoxbearers() {
        // When this creature dies, create a 1/1 white Ally creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Ally", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.ALLY), Set.of(), Set.of()));
    }
}
