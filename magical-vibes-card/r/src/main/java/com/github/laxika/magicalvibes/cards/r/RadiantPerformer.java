package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "54")
public class RadiantPerformer extends Card {

    public RadiantPerformer() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryHasTargetPredicate(),
                        new StackEntryIsSingleTargetPredicate(),
                        new StackEntryAnyOfPredicate(List.of(
                                new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate()),
                                new StackEntryTargetsAnyPlayerPredicate()))
                )),
                "Target must be a spell or ability that targets only one permanent or player."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new CastFromZone(Zone.HAND),
                        CopySpellForEachOtherPermanentOrPlayerEffect.permanentsAndPlayersOnly()));
    }
}
