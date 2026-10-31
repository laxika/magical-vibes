package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DKA", collectorNumber = "23")
public class SuddenDisappearance extends Card {

    public SuddenDisappearance() {
        addSpellEffect(FlickerEffect.exilePlayersPermanentsReturnAtStep(
                new PermanentNotPredicate(new PermanentIsLandPredicate()), TurnStep.END_STEP));
    }
}
