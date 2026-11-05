package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;

@CardRegistration(set = "HOB", collectorNumber = "92")
public class DesertWereWorm extends Card {

    public DesertWereWorm() {
        addStaticEffect(new BoostSelfEffect(
                new Scaled(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER), 2),
                new Fixed(0)));

        // "Whenever you attack with creatures with total power 12 or greater for the first time each turn": the
        // total power is the event condition, "for the first time each turn" counts the player's attacks.
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new AttackingCreaturesTotalPowerAtLeast(12))
                        .withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentIsAttackingPredicate()),
                        new AdditionalCombatPhaseEffect(1)));
    }
}
