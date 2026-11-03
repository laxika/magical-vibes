package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VandalsEdit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "45")
@CardRegistration(set = "SOC", collectorNumber = "93")
public class DefacingDuskmageVandalsEdit extends Card {

    public DefacingDuskmageVandalsEdit() {
        setBackFaceCard(new VandalsEdit());

        addTrigger(Triggers.opponentDrawsSecondCardEachTurn(),
                new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "VandalsEdit";
    }
}
