package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "28")
public class SpikedCorridorTorturePit extends Card {

    public SpikedCorridorTorturePit() {
        setRoomDoorManaCosts(List.of("{3}{R}", "{3}{R}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Spiked Corridor", List.of())
                        .withManaCost("{3}{R}"),
                new ChooseOneEffect.ChooseOneOption("Torture Pit", List.of())
                        .withManaCost("{3}{R}")
        )));

        Map<EffectSlot, CardEffect> devilTokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0,
                        new CreateTokenEffect(
                                3, "Devil", 1, 1, CardColor.RED,
                                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), devilTokenEffects)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new AdditionalControllerDamageToOpponentsEffect(2, true)));
    }
}
