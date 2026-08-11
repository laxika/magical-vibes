package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "241")
public class CoalitionVictory extends Card {

    public CoalitionVictory() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AllOf(List.of(
                        controlsLandOfType(CardSubtype.PLAINS),
                        controlsLandOfType(CardSubtype.ISLAND),
                        controlsLandOfType(CardSubtype.SWAMP),
                        controlsLandOfType(CardSubtype.MOUNTAIN),
                        controlsLandOfType(CardSubtype.FOREST),
                        controlsCreatureOfColor(CardColor.WHITE),
                        controlsCreatureOfColor(CardColor.BLUE),
                        controlsCreatureOfColor(CardColor.BLACK),
                        controlsCreatureOfColor(CardColor.RED),
                        controlsCreatureOfColor(CardColor.GREEN))),
                new WinGameEffect()));
    }

    private static Condition controlsLandOfType(CardSubtype subtype) {
        return new ControlsPermanent(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSubtypePredicate(subtype))));
    }

    private static Condition controlsCreatureOfColor(CardColor color) {
        return new ControlsPermanent(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(color)))));
    }
}
