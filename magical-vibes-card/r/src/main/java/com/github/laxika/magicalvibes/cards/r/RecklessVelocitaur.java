package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;

import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "144")
public class RecklessVelocitaur extends Card {

    public RecklessVelocitaur() {
        // "that Mount or Vehicle gets +2/+0 and gains trample until end of turn" (the crewed or saddled permanent).
        addEffect(EffectSlot.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        new Fixed(2), new Fixed(0), Set.of(Keyword.TRAMPLE)));
    }
}
