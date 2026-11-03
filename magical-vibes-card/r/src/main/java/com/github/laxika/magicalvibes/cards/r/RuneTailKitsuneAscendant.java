package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "27")
public class RuneTailKitsuneAscendant extends Card {

    public RuneTailKitsuneAscendant() {
        setBackFaceCard(new RuneTailsEssence());

        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) >= 30),
                new TransformToBackFaceEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "RuneTailsEssence";
    }
}
