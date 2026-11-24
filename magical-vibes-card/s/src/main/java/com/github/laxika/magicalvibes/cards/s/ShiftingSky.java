package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;

@CardRegistration(set = "8ED", collectorNumber = "100")
@CardRegistration(set = "PLS", collectorNumber = "32")
public class ShiftingSky extends Card {

    public ShiftingSky() {
        // As this enchantment enters, choose a color.
        addStaticEffect(new ChooseColorOnEnterEffect());
        // All nonland permanents are the chosen color.
        addStaticEffect(GrantColorEffect.chosenColor(Subjects.allPermanents(),
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
