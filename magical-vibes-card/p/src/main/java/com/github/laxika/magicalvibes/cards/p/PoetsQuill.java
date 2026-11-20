package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "82")
public class PoetsQuill extends Card {

    public PoetsQuill() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LearnEffect());
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}{B}"));
    }
}
