package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "67")
public class MirrorRoomFracturedRealm extends Card {

    public MirrorRoomFracturedRealm() {
        setRoomDoorManaCosts(List.of("{2}{U}", "{5}{U}{U}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Mirror Room", List.of())
                        .withManaCost("{2}{U}"),
                new ChooseOneEffect.ChooseOneOption("Fractured Realm", List.of())
                        .withManaCost("{5}{U}{U}")
        )));

        target(TargetFilters.creatureYouControl(), 0, 1).addEffect(
                EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0,
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(CardSubtype.REFLECTION), Set.of(), null, null, Map.of())));

        addEffect(EffectSlot.STATIC,
                new AdditionalTriggeredAbilityEffect(
                        new PermanentTruePredicate(), new SourceRoomDoorUnlocked(1), true, false));
    }
}
