package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "15")
public class GrandEntrywayElegantRotunda extends Card {

    public GrandEntrywayElegantRotunda() {
        setRoomDoorManaCosts(List.of("{1}{W}", "{2}{W}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Grand Entryway", List.of())
                        .withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption("Elegant Rotunda", List.of())
                        .withManaCost("{2}{W}")
        )));

        CreateTokenEffect glimmerToken = new CreateTokenEffect(
                1, "Glimmer", 1, 1, CardColor.WHITE, List.of(CardSubtype.GLIMMER), Set.of(),
                Set.of(CardType.ENCHANTMENT));
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0, glimmerToken));

        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
