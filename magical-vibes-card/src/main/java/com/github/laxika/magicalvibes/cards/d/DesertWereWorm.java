package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "92")
public class DesertWereWorm extends Card {

    public DesertWereWorm() {
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Scaled(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER), 2),
                new Fixed(0)));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(12),
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES),
                        new AdditionalCombatPhaseEffect(1)))));
    }
}
