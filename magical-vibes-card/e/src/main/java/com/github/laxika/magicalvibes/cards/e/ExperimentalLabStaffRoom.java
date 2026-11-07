package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "33")
public class ExperimentalLabStaffRoom extends Card {

    public ExperimentalLabStaffRoom() {
        setRoomDoorManaCosts(List.of("{3}{G}", "{2}{G}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Experimental Lab", List.of())
                        .withManaCost("{3}{G}"),
                new ChooseOneEffect.ChooseOneOption("Staff Room", List.of())
                        .withManaCost("{2}{G}")
        )));

        addTrigger(Triggers.doorUnlocked(0),
                SequenceEffect.of(
                        ManifestDreadEffect.forController(),
                        new PutCountersOnChosenPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new PutCountersOnChosenPermanentEffect(CounterType.TRAMPLE, 1)));

        addStaticEffect(new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                                new ChooseOneEffect(List.of(
                                        new ChooseOneEffect.ChooseOneOption("Turn that creature face up",
                                                TurnSourceFaceUpEffect.ofTriggeringPermanent()),
                                        new ChooseOneEffect.ChooseOneOption("Put a +1/+1 counter on it",
                                                new PutCounterOnReferencedPermanentEffect(
                                                        PermanentReference.TRIGGERING,
                                                        CounterType.PLUS_ONE_PLUS_ONE))))),
                        GrantScope.SELF)));
    }
}
