package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "332")
public class LoxodonWarhammer extends Card {

    public LoxodonWarhammer() {
        addEffect(EffectSlot.STATIC, new BoostEquippedCreatureEffect(3, 0));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, Scope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.LIFELINK, Scope.EQUIPPED_CREATURE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new EquipEffect()),
                true,
                false,
                "Equip {3}",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                ),
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
