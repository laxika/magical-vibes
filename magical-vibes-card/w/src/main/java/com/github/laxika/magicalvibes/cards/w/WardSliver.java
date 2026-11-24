package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "617")
public class WardSliver extends Card {

    public WardSliver() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(ProtectionFromColorsEffect.chosenColor(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER))));
    }
}
