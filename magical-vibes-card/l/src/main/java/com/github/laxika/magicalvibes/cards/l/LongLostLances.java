package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "7")
public class LongLostLances extends Card {

    public LongLostLances() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), Subjects.creaturesYouControl(),
                        new PermanentIsEquippedPredicate())));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
