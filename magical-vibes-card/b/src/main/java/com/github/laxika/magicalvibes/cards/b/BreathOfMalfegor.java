package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ARB", collectorNumber = "35")
@CardRegistration(set = "SCD", collectorNumber = "218")
public class BreathOfMalfegor extends Card {

    public BreathOfMalfegor() {
        addSpellEffect(new DealDamageToPlayersEffect(5, Subjects.eachOpponent()));
    }
}
