package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M3C", collectorNumber = "60")
@CardRegistration(set = "M3C", collectorNumber = "112")
public class SawhornNemesis extends Card {

    public SawhornNemesis() {
        addStaticEffect(new ChoosePlayerOnEnterEffect());
        addStaticEffect(new DoubleDamageToOpponentsAndTheirPermanentsEffect(Subjects.chosenPlayer()));
    }
}
