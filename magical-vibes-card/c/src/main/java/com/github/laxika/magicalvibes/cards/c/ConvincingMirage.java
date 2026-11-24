package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M10", collectorNumber = "46")
public class ConvincingMirage extends Card {

    public ConvincingMirage() {
        target(TargetFilters.land())
                .addStaticEffect(new ChooseBasicLandTypeOnEnterEffect())
                .addStaticEffect(GrantSubtypeEffect.chosenSubtype(Subjects.attached(), true, null));
    }
}
