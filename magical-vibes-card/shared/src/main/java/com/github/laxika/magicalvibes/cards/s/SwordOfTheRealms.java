package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class SwordOfTheRealms extends Card {

    public SwordOfTheRealms() {
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new ReturnTriggeringCardToOwnerHandEffect());
        addActivatedAbility(new EquipActivatedAbility("{1}{W}"));
    }
}
