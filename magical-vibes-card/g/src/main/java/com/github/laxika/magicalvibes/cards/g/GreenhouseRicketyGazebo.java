package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "181")
public class GreenhouseRicketyGazebo extends Card {

    public GreenhouseRicketyGazebo() {
        setRoomDoorManaCosts(List.of("{2}{G}", "{3}{G}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Greenhouse", List.of())
                        .withManaCost("{2}{G}"),
                new ChooseOneEffect.ChooseOneOption("Rickety Gazebo", List.of())
                        .withManaCost("{3}{G}")
        )));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(), GrantScope.OWN_LANDS)));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1,
                        new MillControllerAndMayReturnMilledPermanentToHandEffect(4, 2)));
    }
}
