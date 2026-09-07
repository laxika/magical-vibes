package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "30")
public class Mimeofacture extends Card {

    public Mimeofacture() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{3}{U}")));
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                "Target must be a permanent an opponent controls"))
                .addEffect(EffectSlot.SPELL,
                        new SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect());
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{3}{U}"));
    }
}
