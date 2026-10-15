package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "174")
public class FeralIncarnation extends Card {

    public FeralIncarnation() {
        // Convoke is granted by the Scryfall-loaded keyword and handled by the casting service.
        // "Create three 3/3 green Beast creature tokens."
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Beast", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.BEAST), Set.of(), Set.of()));
    }
}
