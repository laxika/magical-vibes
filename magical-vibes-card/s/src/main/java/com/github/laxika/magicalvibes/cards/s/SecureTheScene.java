package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "35")
public class SecureTheScene extends Card {

    public SecureTheScene() {
        target(TargetFilters.nonlandPermanent()).addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(CreateTokenEffect.whiteSoldier(1).forController(Subjects.controllerOf(Subjects.target())));
    }
}
