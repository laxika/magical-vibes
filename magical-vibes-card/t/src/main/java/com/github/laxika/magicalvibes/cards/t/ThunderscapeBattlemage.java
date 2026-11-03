package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "75")
@CardRegistration(set = "DDE", collectorNumber = "41")
public class ThunderscapeBattlemage extends Card {

    public ThunderscapeBattlemage() {
        addStaticEffect(new KickerEffect("{1}{B}"));
        // "Kicker {1}{B} and/or {G}": paying the {G} kicker is a multikicker payment, so it kicks the spell.
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{G}"), true, 1));

        targetWhenKicked(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 0, 1, 1).addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));

        targetWithDynamicCount(new RepeatedAdditionalCostCount("{G}"), new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(), "Target must be an enchantment"), 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerMode.INDEPENDENT,
                        TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{G}")),
                        new DestroyTargetPermanentEffect());
    }
}
