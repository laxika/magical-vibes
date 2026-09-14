package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LGN", collectorNumber = "37")
@CardRegistration(set = "DDN", collectorNumber = "51")
public class EchoTracer extends Card {

    public EchoTracer() {
        addMorph("{2}{U}");
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_TURNED_FACE_UP, ReturnToHandEffect.target());
    }
}
