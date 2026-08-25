package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;

@CardRegistration(set = "TSP", collectorNumber = "50")
public class BrineElemental extends Card {

    public BrineElemental() {
        addMorph("{5}{U}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new SkipNextEffect(SkipKind.UNTAP_STEP, SkipRecipient.EACH_OPPONENT));
    }
}
