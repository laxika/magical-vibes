package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;

@CardRegistration(set = "LEG", collectorNumber = "51")
public class DreamCoat extends Card {

    public DreamCoat() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(BecomeChosenColorsUntilEndOfTurnEffect.chosenColors(
                        Subjects.attachedCreature(), Duration.PERMANENT)),
                "{0}: Enchanted creature becomes the color or colors of your choice. Activate only once each turn.",
                1
        ));
    }
}
