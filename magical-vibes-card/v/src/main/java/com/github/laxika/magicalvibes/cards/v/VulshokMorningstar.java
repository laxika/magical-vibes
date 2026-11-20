package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "9ED", collectorNumber = "315")
@CardRegistration(set = "DST", collectorNumber = "157")
@CardRegistration(set = "DDI", collectorNumber = "66")
public class VulshokMorningstar extends Card {

    public VulshokMorningstar() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
