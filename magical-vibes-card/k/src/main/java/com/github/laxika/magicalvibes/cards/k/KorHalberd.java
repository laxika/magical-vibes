package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MOM", collectorNumber = "27")
public class KorHalberd extends Card {

    public KorHalberd() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.attachedCreature()));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
