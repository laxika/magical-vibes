package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureYouControlTargetFilter;

import java.util.List;

public class LoxodonWarhammer extends Card {

    public LoxodonWarhammer() {
        addEffect(EffectSlot.STATIC, new BoostEquippedCreatureEffect(3, 0));
        addEffect(EffectSlot.STATIC, new GrantKeywordToEquippedCreatureEffect(Keyword.TRAMPLE));
        addEffect(EffectSlot.STATIC, new GrantKeywordToEquippedCreatureEffect(Keyword.LIFELINK));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new EquipEffect()),
                true,
                false,
                "Equip {3}",
                new CreatureYouControlTargetFilter(),
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
