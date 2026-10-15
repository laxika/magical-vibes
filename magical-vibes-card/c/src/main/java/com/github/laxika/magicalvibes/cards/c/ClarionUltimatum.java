package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect;

@CardRegistration(set = "ALA", collectorNumber = "163")
public class ClarionUltimatum extends Card {

    public ClarionUltimatum() {
        // Choose five permanents you control. For each of those permanents, you may search your
        // library for a card with the same name as that permanent. Put those cards onto the
        // battlefield tapped, then shuffle.
        addEffect(EffectSlot.SPELL,
                new ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect());
    }
}
