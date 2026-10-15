package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastFromLibraryWhileSearchingEffect;

@CardRegistration(set = "CSP", collectorNumber = "116")
public class PanglacialWurm extends Card {

    public PanglacialWurm() {
        addEffect(EffectSlot.STATIC, new CastFromLibraryWhileSearchingEffect());
    }
}
