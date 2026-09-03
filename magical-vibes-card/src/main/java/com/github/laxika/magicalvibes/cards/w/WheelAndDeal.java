package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ONS", collectorNumber = "121")
public class WheelAndDeal extends Card {

    public WheelAndDeal() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ), 0, 99)
                .addEffect(EffectSlot.SPELL, new DiscardHandEffect(DiscardRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(7, false, true));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
