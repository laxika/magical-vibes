package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "256")
public class LureboundScarecrow extends Card {

    public LureboundScarecrow() {
        // "As this creature enters, choose a color."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());

        // "When you control no permanents of the chosen color, sacrifice this creature."
        // — State-triggered ability (MTG rule 603.8).
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    CardColor chosenColor = sourcePermanent.getChosenColor();
                    if (chosenColor == null) return false;
                    List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                    if (battlefield == null) return true;
                    return battlefield.stream()
                            .noneMatch(p -> p.getEffectiveColors().contains(chosenColor));
                },
                List.of(new SacrificeSelfEffect()),
                "Lurebound Scarecrow's state-triggered ability"
        ));
    }
}
