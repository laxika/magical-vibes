package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "10E", collectorNumber = "332")
@CardRegistration(set = "C14", collectorNumber = "247")
@CardRegistration(set = "9ED", collectorNumber = "303")
@CardRegistration(set = "MRD", collectorNumber = "201")
@CardRegistration(set = "DPA", collectorNumber = "94")
@CardRegistration(set = "DDG", collectorNumber = "31")
@CardRegistration(set = "HOP", collectorNumber = "118")
@CardRegistration(set = "SLD", collectorNumber = "1034")
@CardRegistration(set = "C15", collectorNumber = "258")
@CardRegistration(set = "C21", collectorNumber = "249")
@CardRegistration(set = "C16", collectorNumber = "261")
@CardRegistration(set = "ONC", collectorNumber = "135")
@CardRegistration(set = "C17", collectorNumber = "216")
@CardRegistration(set = "CM2", collectorNumber = "199")
public class LoxodonWarhammer extends Card {

    public LoxodonWarhammer() {
        addStaticEffect(new StaticBoostEffect(3, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
