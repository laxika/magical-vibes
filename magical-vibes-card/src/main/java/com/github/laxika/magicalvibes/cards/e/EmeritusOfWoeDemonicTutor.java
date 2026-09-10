package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DemonicTutor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreCreaturesDiedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/** Emeritus of Woe // Demonic Tutor (SOS 80). */
@CardRegistration(set = "SOS", collectorNumber = "80")
public class EmeritusOfWoeDemonicTutor extends Card {

    public EmeritusOfWoeDemonicTutor() {
        setBackFaceCard(new DemonicTutor());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new TwoOrMoreCreaturesDiedThisTurn(), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "DemonicTutor";
    }
}
