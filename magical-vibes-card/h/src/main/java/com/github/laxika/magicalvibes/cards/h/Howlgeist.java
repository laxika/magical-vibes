package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;

@CardRegistration(set = "AVR", collectorNumber = "182")
public class Howlgeist extends Card {

    public Howlgeist() {
        // Creatures with power less than this creature's power can't block it.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());

        // Undying is keyword-driven; the engine handles the return in PermanentRemovalService.
    }
}
