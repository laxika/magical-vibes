package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GPT", collectorNumber = "6")
@CardRegistration(set = "RVR", collectorNumber = "19")
@CardRegistration(set = "RVR", collectorNumber = "308")
public class Ghostway extends Card {

    public Ghostway() {
        addSpellEffect(new FlickerEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate()),
                null,
                ReturnTiming.AT_STEP,
                TurnStep.END_STEP,
                false,
                null,
                null,
                0,
                false,
                false));
    }
}
