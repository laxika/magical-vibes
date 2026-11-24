package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "12")
public class ChoMannosBlessing extends Card {

    public ChoMannosBlessing() {
        target(TargetFilters.creature())
                .addStaticEffect(new ChooseColorOnEnterEffect())
                .addStaticEffect(ProtectionFromColorsEffect.chosenColor(Subjects.attachedCreature()));
    }
}
