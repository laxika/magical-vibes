package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "CHK", collectorNumber = "115")
public class HideousLaughter extends Card {

    public HideousLaughter() {
        // All creatures get -2/-2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));

        // Splice onto Arcane {3}{B}{B}
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{3}{B}{B}"));
    }
}
