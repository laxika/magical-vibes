package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "79")
public class UnderwaterTunnelSlimyAquarium extends Card {

    public UnderwaterTunnelSlimyAquarium() {
        setRoomDoorManaCosts(List.of("{U}", "{3}{U}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Underwater Tunnel", List.of())
                        .withManaCost("{U}"),
                new ChooseOneEffect.ChooseOneOption("Slimy Aquarium", List.of())
                        .withManaCost("{3}{U}")
        )));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0, new SurveilEffect(2)));
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1, SequenceEffect.of(
                        ManifestDreadEffect.forController(),
                        new PutCountersOnChosenPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))));
    }
}
