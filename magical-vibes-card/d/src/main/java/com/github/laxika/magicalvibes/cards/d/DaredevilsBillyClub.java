package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "686")
public class DaredevilsBillyClub extends Card {

    public DaredevilsBillyClub() {
        addSimultaneousTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.toAnyTarget(2),
                new DealDamageToPlayersEffect(1, Subjects.you()));
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
