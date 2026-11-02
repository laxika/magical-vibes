package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "68")
public class PsychicBattle extends Card {

    public PsychicBattle() {
        addTrigger(Triggers.playerChoosesTargets(), new PsychicBattleEffect());
    }
}
