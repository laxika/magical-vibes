package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;

@CardRegistration(set = "MIR", collectorNumber = "84")
public class PrismaticLace extends Card {

    public PrismaticLace() {
        // Target permanent becomes the color or colors of your choice. The controller picks the
        // colors on resolution and they replace the permanent's colors indefinitely (CR 105.3).
        target(TargetFilters.permanent()).addSpellEffect(BecomeChosenColorsUntilEndOfTurnEffect.chosenColors(Subjects.target(), Duration.PERMANENT));
    }
}
