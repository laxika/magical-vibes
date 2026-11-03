package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "256")
public class LureboundScarecrow extends Card {

    public LureboundScarecrow() {
        // "As this creature enters, choose a color."
        addStaticEffect(new ChooseColorOnEnterEffect());

        // "When you control no permanents of the chosen color, sacrifice this creature."
        // — State-triggered ability (MTG rule 603.8).
        addTrigger(Triggers.state(
                        (gameData, sourcePermanent, controllerId) -> sourcePermanent.getChosenColor() != null)
                        .withBattlefieldAtMost(0, new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentHasSourceChosenColorPredicate()))),
                new SacrificeSelfEffect());
    }
}
