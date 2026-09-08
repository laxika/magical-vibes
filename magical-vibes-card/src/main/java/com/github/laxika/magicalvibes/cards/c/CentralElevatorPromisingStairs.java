package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctUnlockedRoomNamesCount;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardDoesNotShareNameWithControlledRoomPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "44")
public class CentralElevatorPromisingStairs extends Card {

    public CentralElevatorPromisingStairs() {
        setRoomDoorManaCosts(List.of("{3}{U}", "{2}{U}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Central Elevator", List.of())
                        .withManaCost("{3}{U}"),
                new ChooseOneEffect.ChooseOneOption("Promising Stairs", List.of())
                        .withManaCost("{2}{U}")
        )));

        SearchLibraryEffect searchRoom = new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.ROOM),
                new CardDoesNotShareNameWithControlledRoomPredicate())));
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0, searchRoom));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                SequenceEffect.of(
                        new SurveilEffect(1),
                        ConditionalEffect.unless(
                                new ControlsDistinctUnlockedRoomNamesCount(8),
                                new WinGameEffect()))));
    }
}
