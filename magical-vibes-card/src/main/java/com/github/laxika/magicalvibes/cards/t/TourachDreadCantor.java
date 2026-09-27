package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MH2", collectorNumber = "102")
@CardRegistration(set = "MH2", collectorNumber = "312")
public class TourachDreadCantor extends Card {

    public TourachDreadCantor() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{B}{B}"));
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                new PutCountersOnSourceEffect(1, 1, 1));

        targetWhenKicked(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ), 0, 0, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(),
                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER, true)));
    }
}
