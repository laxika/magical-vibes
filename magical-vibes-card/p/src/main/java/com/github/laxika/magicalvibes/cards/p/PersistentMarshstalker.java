package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "104")
public class PersistentMarshstalker extends Card {

    public PersistentMarshstalker() {
        PermanentCount otherRats = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.RAT), CountScope.CONTROLLER, true);
        addStaticEffect(new DynamicStaticBoostEffect(
                otherRats, new Fixed(0), GrantScope.SELF));

        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT
                        .withEventCondition(new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.RAT)))
                        .withInterveningIf(new GraveyardCardThreshold(7, null)),
                new MayPayManaEffect("{2}{B}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .enterAttacking(true)
                                .build(),
                        "Pay {2}{B} to return Persistent Marshstalker from your graveyard to the battlefield tapped and attacking?"));
    }
}
