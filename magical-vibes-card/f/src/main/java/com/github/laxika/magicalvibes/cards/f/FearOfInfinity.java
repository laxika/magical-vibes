package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "214")
public class FearOfInfinity extends Card {

    public FearOfInfinity() {
        addStaticEffect(new CantBlockEffect());

        MayEffect returnToHand = new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                "Return Fear of Infinity from your graveyard to your hand?");
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD),
                new TriggerCondition(Triggers.youFullyUnlockRoom(), AbilityZones.GRAVEYARD)),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(returnToHand))));
    }
}
