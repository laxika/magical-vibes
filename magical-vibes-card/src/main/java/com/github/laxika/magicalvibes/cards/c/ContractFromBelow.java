package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnteTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SUM", collectorNumber = "97")
public class ContractFromBelow extends Card {

    public ContractFromBelow() {
        // The deck-construction instruction about ante is not an in-game effect.
        addEffect(EffectSlot.SPELL, new DiscardHandEffect());
        addEffect(EffectSlot.SPELL, new AnteTopCardEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(7));
    }
}
