package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOT", collectorNumber = "13")
@CardRegistration(set = "BOT", collectorNumber = "27")
public class OptimusPrimeHero extends Card {

    public OptimusPrimeHero() {
        setBackFaceCard(new OptimusPrimeAutobotLeader());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{U}{R}{W}"));

        addTrigger(Triggers.endStep(ActivePlayer.ANY), new BolsterEffect(1));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ReturnSourceTransformedFromGraveyardEffect(false, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "OptimusPrimeAutobotLeader";
    }
}
