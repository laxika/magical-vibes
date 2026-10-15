package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "27")
public class RuneTailKitsuneAscendant extends Card {

    public RuneTailKitsuneAscendant() {
        setBackFaceCard(new RuneTailsEssence());

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) >= 30,
                List.of(new TransformToBackFaceEffect()),
                "Rune-Tail, Kitsune Ascendant's state-triggered ability"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "RuneTailsEssence";
    }
}
