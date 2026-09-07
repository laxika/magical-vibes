package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfOpponentLibraryAndBecomeCopyEffect;

@CardRegistration(set = "FUT", collectorNumber = "58")
public class ShapeshiftersMarrow extends Card {

    public ShapeshiftersMarrow() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new RevealTopCardOfOpponentLibraryAndBecomeCopyEffect());
    }
}
