package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GN2", collectorNumber = "4")
@CardRegistration(set = "MKC", collectorNumber = "153")
public class FiendishDuo extends Card {

    public FiendishDuo() {
        addStaticEffect(DoubleDamageToOpponentsAndTheirPermanentsEffect.toPlayersOnly(Subjects.eachOpponent()));
    }
}
