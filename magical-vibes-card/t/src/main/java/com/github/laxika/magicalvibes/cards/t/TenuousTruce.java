package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "87")
@CardRegistration(set = "NCC", collectorNumber = "95")
@CardRegistration(set = "BLC", collectorNumber = "159")
public class TenuousTruce extends Card {

    public TenuousTruce() {
        setEnchantPlayer(true);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));

        addTrigger(Triggers.endStep(ActivePlayer.ENCHANTED_PLAYER),
                new ConditionalEffect(new TargetPlayerIsActive(), SequenceEffect.of(
                        new DrawCardEffect(),
                        new DrawCardForTargetPlayerEffect(1))));
        addTrigger(Triggers.playerAttacks(),
                new ConditionalEffect(
                        new ControllerAndEnchantedPlayerAttackEachOther(),
                        new SacrificeSelfEffect()));
    }
}
