package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "3")
public class IntroductionToAnnihilation extends Card {

    public IntroductionToAnnihilation() {
        target(TargetFilters.nonlandPermanent()).addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new DrawCardEffect(1, Subjects.controllerOf(Subjects.target())));
    }
}
