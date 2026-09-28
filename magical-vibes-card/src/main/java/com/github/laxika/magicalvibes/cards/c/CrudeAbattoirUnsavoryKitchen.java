package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "13")
public class CrudeAbattoirUnsavoryKitchen extends Card {

    public CrudeAbattoirUnsavoryKitchen() {
        setRoomDoorManaCosts(List.of("{R}", "{2}{R}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Crude Abattoir", List.of())
                        .withManaCost("{R}"),
                new ChooseOneEffect.ChooseOneOption("Unsavory Kitchen", List.of())
                        .withManaCost("{2}{R}")
        )));

        target(TargetFilters.creature(), 0, 1).addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0,
                        new DealDamageToTargetCreatureEffect(2)));

        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE,
                new ConditionalEffect(
                        new SourceRoomDoorUnlocked(1),
                        new PerpetuallyBoostCreatureCardInHandEffect(2, Set.of(Keyword.HASTE))));
    }
}
