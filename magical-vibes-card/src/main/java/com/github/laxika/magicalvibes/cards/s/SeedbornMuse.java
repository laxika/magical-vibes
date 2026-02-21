package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;

@CardRegistration(set = "10E", collectorNumber = "296")
public class SeedbornMuse extends Card {

    public SeedbornMuse() {
        addEffect(EffectSlot.STATIC, new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP));
    }
}
