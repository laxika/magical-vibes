package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeEqualToSpellManaValueCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "17")
public class CrampedVentsAccessMaze extends Card {

    public CrampedVentsAccessMaze() {
        setRoomDoorManaCosts(List.of("{3}{B}", "{5}{B}{B}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Cramped Vents", List.of())
                        .withManaCost("{3}{B}"),
                new ChooseOneEffect.ChooseOneOption("Access Maze", List.of())
                        .withManaCost("{5}{B}{B}")
        )));

        target(TargetFilters.creatureAnOpponentControls()).addEffect(
                EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0, SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(6),
                        new GainLifeEffect(new EventValue()))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new AlternativeCostForSpellsEffect(
                        null, null, null, true, true, false, false, true,
                        Set.of(Zone.HAND), new PayLifeEqualToSpellManaValueCost(), null)));
    }
}
