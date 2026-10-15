package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "75")
public class SavageConception extends Card {

    public SavageConception() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(1, "Beast", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.BEAST), Set.of(), Set.of()));
        addCastingOption(new Retrace());
    }
}
