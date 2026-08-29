package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "29")
public class SpiritualVisit extends Card {

    public SpiritualVisit() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{W}"));
    }
}
