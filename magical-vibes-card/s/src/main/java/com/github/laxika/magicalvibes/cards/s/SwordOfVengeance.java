package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M11", collectorNumber = "216")
@CardRegistration(set = "GN3", collectorNumber = "120")
@CardRegistration(set = "C14", collectorNumber = "276")
@CardRegistration(set = "C15", collectorNumber = "272")
@CardRegistration(set = "NEC", collectorNumber = "164")
@CardRegistration(set = "C17", collectorNumber = "228")
public class SwordOfVengeance extends Card {

    public SwordOfVengeance() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
