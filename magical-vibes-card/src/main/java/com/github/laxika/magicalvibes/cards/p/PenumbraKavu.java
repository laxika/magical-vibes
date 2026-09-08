package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "83")
public class PenumbraKavu extends Card {

    public PenumbraKavu() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Kavu", 3, 3, CardColor.BLACK, List.of(CardSubtype.KAVU), Set.of(), Set.of()));
    }
}
