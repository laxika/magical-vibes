package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CHK", collectorNumber = "264")
public class NoDachi extends Card {

    public NoDachi() {
        // Equipped creature gets +2/+0 and has first strike.
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature()));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
