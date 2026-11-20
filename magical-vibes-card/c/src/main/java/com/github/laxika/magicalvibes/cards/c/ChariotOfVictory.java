package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JOU", collectorNumber = "159")
public class ChariotOfVictory extends Card {

    public ChariotOfVictory() {
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
