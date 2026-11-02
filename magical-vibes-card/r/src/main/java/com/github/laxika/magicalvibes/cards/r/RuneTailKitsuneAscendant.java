package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "27")
public class RuneTailKitsuneAscendant extends Card {

    public RuneTailKitsuneAscendant() {
        setBackFaceCard(new RuneTailsEssence());

        addTrigger(Triggers.state(), new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) >= 30,
                List.of(new TransformToBackFaceEffect(true)),
                "Rune-Tail, Kitsune Ascendant's state-triggered ability"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "RuneTailsEssence";
    }
}
