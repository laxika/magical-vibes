package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "129")
public class CharredFoyerWarpedSpace extends Card {

    public CharredFoyerWarpedSpace() {
        setRoomDoorManaCosts(List.of("{3}{R}", "{4}{R}{R}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Charred Foyer", List.of())
                        .withManaCost("{3}{R}"),
                new ChooseOneEffect.ChooseOneOption("Warped Space", List.of())
                        .withManaCost("{4}{R}{R}")
        )));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new ExileTopCardMayPlayThisTurnEffect(false)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new AlternativeCostForSpellsEffect(
                        "{0}", null, null, true, false, false, false, false,
                        Set.of(Zone.EXILE), null, null)));
    }
}
