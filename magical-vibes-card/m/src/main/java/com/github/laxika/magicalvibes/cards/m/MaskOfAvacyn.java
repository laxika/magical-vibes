package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ISD", collectorNumber = "229")
public class MaskOfAvacyn extends Card {

    public MaskOfAvacyn() {
        addStaticEffect(new StaticBoostEffect(1, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
