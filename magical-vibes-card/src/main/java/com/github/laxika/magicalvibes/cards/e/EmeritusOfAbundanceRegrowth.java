package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

/** Emeritus of Abundance // Regrowth (SOS 145). */
@CardRegistration(set = "SOS", collectorNumber = "145")
public class EmeritusOfAbundanceRegrowth extends Card {

    public EmeritusOfAbundanceRegrowth() {
        setBackFaceCard(new Regrowth());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Regrowth";
    }
}
