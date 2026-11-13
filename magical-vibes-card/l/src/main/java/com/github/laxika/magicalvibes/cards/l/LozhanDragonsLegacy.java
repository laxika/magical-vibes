package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "240")
public class LozhanDragonsLegacy extends Card {

    public LozhanDragonsLegacy() {
        TargetFilter nonCommanderAnyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentIsBattlePredicate()
                        )),
                        new PermanentNotPredicate(new PermanentIsCommanderPredicate())
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must not be a commander"
        );
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(new TargetSpellManaValue());

        // "Whenever you cast an Adventure spell or a Dragon spell": two trigger conditions of the one ability's
        // target, each its own ability of the same text.
        var target = target(nonCommanderAnyTarget);
        target.addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardSubtypePredicate(CardSubtype.DRAGON)),
                damage);
        target.addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                damage);
    }
}
