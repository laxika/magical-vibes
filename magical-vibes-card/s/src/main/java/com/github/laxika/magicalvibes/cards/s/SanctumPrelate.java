package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsWithChosenManaValueCantBeCastEffect;

@CardRegistration(set = "SLD", collectorNumber = "278")
public class SanctumPrelate extends Card {

    public SanctumPrelate() {
        addStaticEffect(new ChooseNumberOnEnterEffect(0, 99));
        addStaticEffect(new NoncreatureSpellsWithChosenManaValueCantBeCastEffect());
    }
}
