package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;

@CardRegistration(set = "INV", collectorNumber = "77")
public class SwayOfIllusion extends Card {

    public SwayOfIllusion() {
        target(TargetFilters.creature(), 0, 99)
                .addSpellEffect(BecomeChosenColorsUntilEndOfTurnEffect.chosenColor(
                        Subjects.target(), Duration.UNTIL_END_OF_TURN));
        addSpellEffect(new DrawCardEffect());
    }
}
