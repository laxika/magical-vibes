package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromTargetHandOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "BLB", collectorNumber = "88")
public class CruelclawsHeist extends Card {

    public CruelclawsHeist() {
        addEffect(EffectSlot.STATIC, new GiftEffect());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        new DrawCardForTargetPlayerEffect(1, false, true)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        new ExileNonlandCardFromTargetHandOrGraveyardEffect(true, true)))
                .addEffect(EffectSlot.SPELL,
                        new ConditionalEffect(new NotCondition(new GiftPromised()),
                                new ExileNonlandCardFromTargetHandOrGraveyardEffect(false, true)));
    }
}
