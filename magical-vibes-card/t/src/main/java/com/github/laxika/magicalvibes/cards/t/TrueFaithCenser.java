package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "267")
@CardRegistration(set = "SIR", collectorNumber = "262")
public class TrueFaithCenser extends Card {

    public TrueFaithCenser() {
        addStaticEffect(new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), Subjects.attachedCreature()));
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature(),
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
