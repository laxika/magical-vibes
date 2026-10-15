package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;

@CardRegistration(set = "BOT", collectorNumber = "13")
@CardRegistration(set = "BOT", collectorNumber = "27")
public class OptimusPrimeHero extends Card {

    public OptimusPrimeHero() {
        setBackFaceCard(new OptimusPrimeAutobotLeader());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{U}{R}{W}"));

        addEffect(EffectSlot.END_STEP_TRIGGERED, new BolsterEffect(1));
        addEffect(EffectSlot.ON_DEATH,
                new ReturnSourceTransformedFromGraveyardEffect(false, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "OptimusPrimeAutobotLeader";
    }
}
