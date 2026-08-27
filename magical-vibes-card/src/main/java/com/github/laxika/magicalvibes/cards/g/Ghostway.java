package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GPT", collectorNumber = "6")
public class Ghostway extends Card {

    public Ghostway() {
        addEffect(EffectSlot.SPELL, new FlickerEffect(
                FlickerScope.CONTROLLERS_PERMANENTS,
                new PermanentIsCreaturePredicate(),
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
