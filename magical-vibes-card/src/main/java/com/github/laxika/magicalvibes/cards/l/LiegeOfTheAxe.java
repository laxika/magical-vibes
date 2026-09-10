package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "LGN", collectorNumber = "16")
public class LiegeOfTheAxe extends Card {

    public LiegeOfTheAxe() {
        addMorph("{1}{W}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
