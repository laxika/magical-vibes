package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ROE", collectorNumber = "220")
public class OgresCleaver extends Card {

    public OgresCleaver() {
        addStaticEffect(new StaticBoostEffect(5, 0, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
