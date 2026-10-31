package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "2")
public class CaduceusStaffOfHermes extends Card {

    public CaduceusStaffOfHermes() {
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.EQUIPPED_CREATURE));

        ControllerLifeAtLeast condition = new ControllerLifeAtLeast(30);
        addStaticEffect(new ConditionalEffect(condition,
                new StaticBoostEffect(5, 5, Set.of(Keyword.INDESTRUCTIBLE), GrantScope.EQUIPPED_CREATURE)));
        addStaticEffect(new ConditionalEffect(condition,
                new PreventAllDamageToAttachedCreatureEffect()));

        addActivatedAbility(new EquipActivatedAbility("{W}{W}"));
    }
}
