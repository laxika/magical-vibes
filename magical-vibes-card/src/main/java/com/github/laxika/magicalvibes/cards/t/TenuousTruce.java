package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerAndEnchantedPlayerAttackEachOther;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActive;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "NCC", collectorNumber = "87")
@CardRegistration(set = "NCC", collectorNumber = "95")
public class TenuousTruce extends Card {

    public TenuousTruce() {
        setEnchantPlayer(true);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));

        addEffect(EffectSlot.ENCHANTED_PLAYER_END_STEP_TRIGGERED,
                new ConditionalEffect(new TargetPlayerIsActive(), SequenceEffect.of(
                        new DrawCardEffect(),
                        new DrawCardForTargetPlayerEffect(1))));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new ControllerAndEnchantedPlayerAttackEachOther(),
                        new SacrificeSelfEffect()));
    }
}
