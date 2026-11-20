package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BRO", collectorNumber = "231")
public class AeronautsWings extends Card {

    public AeronautsWings() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
