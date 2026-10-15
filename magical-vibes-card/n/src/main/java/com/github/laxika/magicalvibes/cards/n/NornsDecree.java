package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttackedPlayerPoisoned;
import com.github.laxika.magicalvibes.model.effect.BatchedCombatDamageToYouTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ONC", collectorNumber = "9")
@CardRegistration(set = "ONC", collectorNumber = "47")
public class NornsDecree extends Card {

    public NornsDecree() {
        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new BatchedCombatDamageToYouTriggerEffect(
                                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PERMANENT_CONTROLLER))));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new AllOf(List.of(new AttackedPlayerPoisoned())),
                        new DrawCardForTriggeringPlayerEffect()));
    }
}
