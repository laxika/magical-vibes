package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;

@CardRegistration(set = "AVR", collectorNumber = "202")
public class WanderingWolf extends Card {

    public WanderingWolf() {
        // Creatures with power less than this creature's power can't block it.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());
    }
}
