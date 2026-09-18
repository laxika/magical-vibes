package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "128")
public class AnimalAttendant extends Card {

    public AnimalAttendant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardAnyColorManaEffect.forNonHumanCreatureCounter(1)),
                "{T}: Add one mana of any color. If that mana is spent to cast a non-Human creature spell, that creature enters with an additional +1/+1 counter on it."
        ));
    }
}
