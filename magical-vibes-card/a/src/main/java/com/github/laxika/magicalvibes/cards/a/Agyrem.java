package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "3")
@CardRegistration(set = "OPCA", collectorNumber = "11")
public class Agyrem extends Card {

    public Agyrem() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect(
                new com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate(java.util.Set.of(CardColor.WHITE)),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, false)));
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect(
                new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(new com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate(java.util.Set.of(CardColor.WHITE))),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null)));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new GrantStaticEffectToPlayerUntilPlaneswalkEffect(
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate()))));
    }
}
