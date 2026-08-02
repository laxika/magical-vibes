package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M15", collectorNumber = "212")
public class AvariceAmulet extends Card {

    public AvariceAmulet() {
        // Equipped creature gets +2/+0 and has vigilance and "At the beginning of your upkeep, draw a card."
        // The draw ability is granted to the creature, so its controller draws — even if an opponent
        // has since taken the Equipment.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.UPKEEP_TRIGGERED, new DrawCardEffect(1), GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, target opponent gains control of this Equipment.
        // The opponent is chosen as the death trigger goes on the stack, not when the artifact is cast.
        setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new TargetPlayerGainsControlOfSourceCreatureEffect());

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
