package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "EOE", collectorNumber = "229")
public class SpaceTimeAnomaly extends Card {

    public SpaceTimeAnomaly() {
        addSpellEffect(new MillEffect(new ControllerLifeTotal(), Subjects.targetPlayer()));
    }
}
