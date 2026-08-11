package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "KTK", collectorNumber = "143")
public class PineWalker extends Card {

    public PineWalker() {
        addMorph("{4}{G}");
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
