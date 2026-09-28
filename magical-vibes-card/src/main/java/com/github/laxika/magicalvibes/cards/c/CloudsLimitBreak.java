package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "14")
@CardRegistration(set = "FIC", collectorNumber = "103")
public class CloudsLimitBreak extends Card {

    public CloudsLimitBreak() {
        PermanentPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        PermanentPredicateTargetFilter tappedCreatureTarget = new PermanentPredicateTargetFilter(
                tappedCreature, "Target must be a tapped creature");

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cross-Slash - {0} - Destroy target tapped creature",
                        new DestroyTargetPermanentEffect(), tappedCreatureTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Blade Beam - {1} - Destroy any number of target tapped creatures with different controllers",
                        List.of(new DestroyEachTargetPermanentEffect(tappedCreature)), tappedCreatureTarget,
                        null, 0, 99, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Omnislash - {3}{W} - Destroy all tapped creatures",
                        new DestroyAllPermanentsEffect(tappedCreature)
        ))));
        addEffect(EffectSlot.SPELL, new TieredManaCost(List.of("", "{1}", "{3}{W}")));
    }
}
