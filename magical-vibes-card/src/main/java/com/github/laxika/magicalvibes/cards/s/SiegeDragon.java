package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "162")
public class SiegeDragon extends Card {

    public SiegeDragon() {
        // Flying is auto-loaded from Scryfall.
        // When this creature enters, destroy all Walls your opponents control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))));

        // Whenever this creature attacks, if defending player controls no Walls, it deals 2 damage
        // to each creature without flying that player controls.
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(
                        new NotCondition(new DefendingPlayerControlsPermanent(
                                new PermanentHasSubtypePredicate(CardSubtype.WALL))),
                        new DealDamageToEachMatchingPermanentEffect(2,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentControlledByDefendingPlayerPredicate(),
                                        new PermanentNotPredicate(
                                                new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                                EachPermanentScope.ALL_PLAYERS)));
    }
}
