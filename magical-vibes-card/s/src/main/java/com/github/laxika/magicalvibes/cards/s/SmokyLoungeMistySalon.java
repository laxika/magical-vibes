package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.UnlockedRoomDoorsCount;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "235")
public class SmokyLoungeMistySalon extends Card {

    public SmokyLoungeMistySalon() {
        setRoomDoorManaCosts(List.of("{2}{R}", "{3}{U}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Smoky Lounge", List.of())
                        .withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption("Misty Salon", List.of())
                        .withManaCost("{3}{U}")
        )));

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new AwardRestrictedManaEffect(
                        ManaColor.RED, 2,
                        new ManaRestriction.RoomSpellsOrUnlocks())));

        DynamicAmount unlockedDoors = new UnlockedRoomDoorsCount(CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1,
                        new CreateTokenEffect(
                                "Spirit", unlockedDoors, unlockedDoors, CardColor.BLUE,
                                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of())));
    }
}
