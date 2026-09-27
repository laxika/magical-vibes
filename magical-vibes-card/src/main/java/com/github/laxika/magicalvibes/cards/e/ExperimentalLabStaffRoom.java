package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "33")
public class ExperimentalLabStaffRoom extends Card {

    public ExperimentalLabStaffRoom() {
        setRoomDoorManaCosts(List.of("{3}{G}", "{2}{G}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Experimental Lab", List.of())
                        .withManaCost("{3}{G}"),
                new ChooseOneEffect.ChooseOneOption("Staff Room", List.of())
                        .withManaCost("{2}{G}")
        )));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0, SequenceEffect.of(
                        ManifestDreadEffect.forController(),
                        new PutCountersOnChosenPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new PutCountersOnChosenPermanentEffect(CounterType.TRAMPLE, 1))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Turn that creature face up", new TurnSourceFaceUpEffect()),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on it",
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))
                )), true));
    }
}
