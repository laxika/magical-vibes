package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ORI", collectorNumber = "96")
public class EyeblightMassacre extends Card {

    public EyeblightMassacre() {
        // "Non-Elf creatures" hits both sides of the battlefield; the subtype check is Changeling-aware.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ELF))));
    }
}
