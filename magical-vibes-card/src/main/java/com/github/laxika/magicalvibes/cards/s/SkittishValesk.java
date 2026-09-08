package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceDownEffect;

@CardRegistration(set = "ONS", collectorNumber = "231")
public class SkittishValesk extends Card {

    public SkittishValesk() {
        addMorph("{5}{R}");
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new FlipCoinWinEffect(null, new TurnSourceFaceDownEffect()));
    }
}
