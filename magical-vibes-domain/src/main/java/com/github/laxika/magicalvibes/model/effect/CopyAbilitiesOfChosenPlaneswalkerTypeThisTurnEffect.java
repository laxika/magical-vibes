package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * "Choose a planeswalker type. Until end of turn, whenever you activate an ability of a planeswalker of that type, copy
 * that ability. You may choose new targets for the copies." (Leori, Sparktouched Hunter): the controller chooses the
 * type as this resolves, and the resolution creates a delayed triggered ability (CR 603.7a) with that type bound into
 * its trigger, which lasts until end of turn whether or not the source is still on the battlefield.
 */
public record CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect() implements CardEffect {

    /** The delayed ability this creates once {@code type} is chosen. */
    public TriggeredAbility delayedAbility(CardSubtype type) {
        return new TriggeredAbility(Triggers.youActivateAbility().withSource(new PermanentAllOfPredicate(List.of(
                new PermanentIsPlaneswalkerPredicate(), new PermanentHasSubtypePredicate(type)))),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}
