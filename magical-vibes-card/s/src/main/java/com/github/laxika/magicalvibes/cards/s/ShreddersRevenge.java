package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "76")
public class ShreddersRevenge extends Card {

    public ShreddersRevenge() {
        var targetPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."
        );
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player discards two cards",
                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER),
                        targetPlayer
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws two cards and loses 2 life",
                        List.of(
                                new DrawCardForTargetPlayerEffect(2, false, true),
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)
                        ),
                        targetPlayer
                )
        )));
    }
}
