package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "41")
@CardRegistration(set = "TMT", collectorNumber = "262")
public class KitsuneDragonsDaughter extends Card {

    public KitsuneDragonsDaughter() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        SpellTarget targets = target(new PermanentPredicateTargetFilter(
                otherCreature, "Targets must be other creatures."), 2, 2);
        targets.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ExchangeControlOfTargetPermanentsEffect.forTwoTargetsInOneGroup(otherCreature),
                "Exchange control of the two target creatures?"));
        targets.addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                ExchangeControlOfTargetPermanentsEffect.forTwoTargetsInOneGroup(otherCreature),
                "Exchange control of the two target creatures?"));
    }
}
