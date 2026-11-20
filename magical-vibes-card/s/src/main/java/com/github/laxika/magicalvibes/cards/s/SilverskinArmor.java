package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MBS", collectorNumber = "132")
public class SilverskinArmor extends Card {

    public SilverskinArmor() {
        // Equipped creature gets +1/+1
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        // Equipped creature is an artifact in addition to its other types
        addStaticEffect(new GrantCardTypeEffect(CardType.ARTIFACT, Subjects.attachedCreature()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
