package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "10E", collectorNumber = "65")
@CardRegistration(set = "TOR", collectorNumber = "23")
public class AmbassadorLaquatus extends Card {

    public AmbassadorLaquatus() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new MillEffect(3, Subjects.targetPlayer())), "{3}: Target player mills three cards."));
    }
}
