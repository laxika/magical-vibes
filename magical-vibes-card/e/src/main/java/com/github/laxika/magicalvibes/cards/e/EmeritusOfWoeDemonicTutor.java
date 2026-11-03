package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DemonicTutor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreCreaturesDiedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Emeritus of Woe // Demonic Tutor (SOS 80). */
@CardRegistration(set = "SOS", collectorNumber = "80")
public class EmeritusOfWoeDemonicTutor extends Card {

    public EmeritusOfWoeDemonicTutor() {
        setBackFaceCard(new DemonicTutor());

        addStaticEffect(new BecomePreparedEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreCreaturesDiedThisTurn()),
                new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DemonicTutor";
    }
}
