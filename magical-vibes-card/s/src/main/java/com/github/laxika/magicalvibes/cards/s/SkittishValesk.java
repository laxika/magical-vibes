package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceDownEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "231")
public class SkittishValesk extends Card {

    public SkittishValesk() {
        addMorph("{5}{R}");
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new FlipCoinWinEffect(null, new TurnSourceFaceDownEffect()));
    }
}
