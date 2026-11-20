package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DOM", collectorNumber = "221")
public class JoustingLance extends Card {

    public JoustingLance() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(new ConditionalEffect(new ControllerTurn(), 
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature())));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
